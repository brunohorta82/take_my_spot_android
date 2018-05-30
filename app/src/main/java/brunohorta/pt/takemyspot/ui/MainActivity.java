package brunohorta.pt.takemyspot.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import brunohorta.pt.takemyspot.R;
import brunohorta.pt.takemyspot.databinding.ActivityMainBinding;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.viewmodel.MainViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding mDataBinding;
    private MainViewModel model;
    private GoogleMap map;
    private boolean reservedSpotMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = ViewModelProviders.of(this, new MainViewModel.Factory(getApplication())).get(MainViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Storage Permissions
            final int REQUEST_EXTERNAL_STORAGE = 1;
            final String[] PERMISSIONS_STORAGE = {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
            };
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            // Workaround for FileUriExposedException in Android >= M
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        mDataBinding.btnTakeSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.registerSpot(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Your spot is registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "You already have a spot under evaluation", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error one problem occurs", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        registerLocationUpdates();
        subscribeInterestingSpot();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDataBinding.ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.dismissCurrentInterestingLocation();
            }
        });
        mDataBinding.ibAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.takeSpot(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "This spot is yours! go grab it before it expires", Toast.LENGTH_SHORT).show();
                            model.markSpotAsReserved();
                        } else {
                            Toast.makeText(getApplicationContext(), "The spot was already taken by someone faster than you!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error one problem occurs", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mDataBinding.setDisableTakeMySpot(true);
        mDataBinding.setSpot(null);
    }

    private void subscribeInterestingSpot() {
        model.getInterestingSpotLiveData().observe(this, new Observer<Spot>() {
            @Override
            public void onChanged(@Nullable Spot spot) {
                verifyCurrentInterestingSpot(spot);
            }

        });
        model.getMySpotTakenLiveData().observe(this, new Observer<Spot>() {
            @Override
            public void onChanged(@Nullable Spot spot) {
                reservedSpotMode = spot != null;
                enterExitReservedSpotMode(spot != null);
                if (reservedSpotMode) {
                    drawTakerRoute(spot.getLatitude(), spot.getLongitude(), spot.getTakerLatitude(), spot.getTakerLongitude());
                } else {
                    verifyCurrentInterestingSpot(model.getInterestingSpot());
                }
            }

        });
    }

    private void verifyCurrentInterestingSpot(Spot spot) {
        if (reservedSpotMode) {
            return;
        }
        if (spot == null || model.getLatitude() <= -9999 || model.getLongitude() <= -9999) {
            mDataBinding.clInterestingSpot.setVisibility(View.GONE);
        } else {
            mDataBinding.clInterestingSpot.setVisibility(View.VISIBLE);
            drawRoute(spot.getLatitude(), spot.getLongitude(), model.getLatitude(), model.getLongitude());
            mDataBinding.setSpot(spot);
        }
    }

    private void drawRoute(double latitude, double longitude, double userLatitude, double userLongitude) {
        if (map != null) {
            map.clear();
            PolylineOptions options = new PolylineOptions()
                    .width(5)
                    .color(Color.RED);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (latitude <= -9999 || longitude <= -9999 || userLatitude <= -9999 || userLongitude <= -9999) {
                return;
            }
            LatLng latLng = new LatLng(latitude, longitude);
            options.add(latLng);
            map.addMarker(
                    new MarkerOptions().position(latLng)
            );
            LatLng userLatLng = new LatLng(userLatitude, userLongitude);
            options.add(userLatLng);
            map.addMarker(
                    new MarkerOptions().position(userLatLng)
            );
            builder.include(latLng);
            builder.include(userLatLng);
            map.addPolyline(options);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
            System.out.println(latLng + "->" + userLatLng);
        }
    }

    private void registerLocationUpdates() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient mLocationClient = new FusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                model.updateLocation(locationResult, new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        System.out.println(response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        System.out.println("error");
                    }
                });
                //mDataBinding.tvLocation.setText(locationResult.getLastLocation() == null ? "-, -" : (locationResult.getLastLocation().getLatitude() + ", " + locationResult.getLastLocation().getLongitude()));
                verifyCurrentInterestingSpot(model.getInterestingSpot());
                mDataBinding.setDisableTakeMySpot(locationResult.getLastLocation() == null);
            }
        }, Looper.myLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void enterExitReservedSpotMode(boolean enter) {
        mDataBinding.btnTakeSpot.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.clInterestingSpot.setVisibility(enter ? View.VISIBLE : View.INVISIBLE);
        mDataBinding.tvTitle.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.tvReserved.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.ibAccept.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.ibClose.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
    }


    private void drawTakerRoute(double latitude, double longitude, double takerLatitude, double takerLongitude) {
        if (map != null) {
            map.clear();
            PolylineOptions options = new PolylineOptions()
                    .width(5)
                    .color(Color.RED);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (latitude <= -9999 || longitude <= -9999 || takerLatitude <= -9999 || takerLongitude <= -9999) {
                return;
            }
            LatLng latLng = new LatLng(latitude, longitude);
            options.add(latLng);
            map.addMarker(
                    new MarkerOptions().position(latLng)
            );
            LatLng userLatLng = new LatLng(takerLatitude, takerLongitude);
            options.add(userLatLng);
            map.addMarker(
                    new MarkerOptions().position(userLatLng)
            );
            builder.include(latLng);
            builder.include(userLatLng);
            map.addPolyline(options);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
            System.out.println(latLng + "->" + userLatLng);
        }
    }
}

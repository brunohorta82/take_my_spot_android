package brunohorta.pt.takemyspot.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

import brunohorta.pt.takemyspot.R;
import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.bluetooth.BeaconScanner;
import brunohorta.pt.takemyspot.bluetooth.BeaconsContants;
import brunohorta.pt.takemyspot.databinding.ActivityMainBinding;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.viewmodel.MainViewModel;
import io.github.krtkush.lineartimer.LinearTimer;
import io.github.krtkush.lineartimer.LinearTimerStates;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, BeaconConsumer {

    private static final int REQUEST_LOCATION = 1;
    private ActivityMainBinding mDataBinding;
    private MainViewModel model;
    private GoogleMap map;
    private boolean reservedSpotMode;

    private BeaconTransmitter mBeaconTransmitter;
    private BeaconScanner mBeaconScanner;

    private Handler mHandler;
    private Runnable mStopScanRunnable;
    private boolean mapLoaded;
    private SupportMapFragment mapFragment;
    private LinearTimer linearTimer;
    private LinearTimer linearTimerMySpot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        model = ViewModelProviders.of(this, new MainViewModel.Factory(getApplication())).get(MainViewModel.class);

        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        }
    }

    private void init() {
        linearTimer = new LinearTimer.Builder()
                .linearTimerView(mDataBinding.linearTimer)
                //.duration(60 * 1000)
                .duration(60 * 1000)
                .build();
        linearTimerMySpot = new LinearTimer.Builder()
                .linearTimerView(mDataBinding.linearTimerMySpot)
                //.duration(60 * 1000)
                .duration(60 * 1000)
                .build();
        mDataBinding.btnTakeSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    linearTimer.resetTimer();
                } catch (Exception e) {
                }
                try {
                    linearTimer = new LinearTimer.Builder()
                            .linearTimerView(mDataBinding.linearTimer)
                            //.duration(60 * 1000)
                            .duration(60 * 1000)
                            .build();
                    linearTimer.startTimer();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                if (model.getLatitude() <= -9999 || model.getLongitude() <= -9999) {
                    Toast.makeText(MainActivity.this, "While the app doesn't retrieve your current location ypu can not offer your spot!", Toast.LENGTH_SHORT).show();
                    return;
                }
                model.registerSpot(new Callback<Spot>() {
                    @Override
                    public void onResponse(@NonNull Call<Spot> call, @NonNull Response<Spot> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Your spot is registered", Toast.LENGTH_SHORT).show();
                            startBeaconMode();
                            mDataBinding.btnTakeSpot.setVisibility(View.GONE);
                            mDataBinding.linearTimerMySpot.setVisibility(View.VISIBLE);
                            try {
                                linearTimerMySpot.resetTimer();
                            } catch (Exception e) {
                            }
                            linearTimerMySpot = new LinearTimer.Builder()
                                    .linearTimerView(mDataBinding.linearTimerMySpot)
                                    //.duration(60 * 1000)
                                    .duration(60 * 1000)
                                    .build();
                            try {
                                linearTimerMySpot.startTimer();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "You already have a spot under evaluation", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Spot> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error one problem occurs", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        registerLocationUpdates();
        subscribeInterestingSpot();
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
                            scanForBeacons();
                            mDataBinding.linearTimer.setVisibility(View.VISIBLE);
                            try {
                                linearTimer.resetTimer();
                            } catch (Exception e) {
                            }
                            try {
                                /*linearTimer = new LinearTimer.Builder()
                                        .linearTimerView(mDataBinding.linearTimer)
                                        //.duration(60 * 1000)
                                        .duration(60 * 1000)
                                        .build();*/
                                linearTimer.startTimer();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
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
        model.dismissCurrentInterestingLocation();
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
                if (spot == null) {
                    try {
                        linearTimerMySpot.pauseTimer();
                    } catch (IllegalStateException e) {
                    }
                    mDataBinding.linearTimerMySpot.setVisibility(View.GONE);
                    mDataBinding.btnTakeSpot.setVisibility(View.VISIBLE);
                }
                reservedSpotMode = spot != null && !spot.isValidated();
                enterExitReservedSpotMode(reservedSpotMode);
                if (reservedSpotMode && spot != null) {
                    drawTakerRoute(spot.getLatitude(), spot.getLongitude(), spot.getTakerLatitude(), spot.getTakerLongitude());
                    mDataBinding.linearTimer.setVisibility(View.VISIBLE);
                    mDataBinding.clInterestingSpot.setVisibility(View.VISIBLE);
                    mDataBinding.clNoSpot.setVisibility(View.INVISIBLE);
                    if (linearTimer.getState() != LinearTimerStates.ACTIVE) {
                        try {
                            linearTimer.resetTimer();
                        } catch (Exception e) {
                        }
                        try {
                            /*linearTimer = new LinearTimer.Builder()
                                    .linearTimerView(mDataBinding.linearTimer)
                                    //.duration(60 * 1000)
                                    .duration(60 * 1000)
                                    .build();*/
                            linearTimer.startTimer();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    //mDataBinding.btnTakeSpot.setEnabled(false);
                } else {
                    mDataBinding.linearTimer.setVisibility(View.INVISIBLE);
                    //mDataBinding.btnTakeSpot.setEnabled(true);
                    verifyCurrentInterestingSpot(model.getInterestingSpot());
                }
                if (spot != null && spot.isValidated()) {
                    model.dismissMyTakenSpot();
                    mDataBinding.linearTimerMySpot.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "The driver who reserved your spot has arrived! Thanks for your time", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    private void verifyCurrentInterestingSpot(Spot spot) {
        if (reservedSpotMode) {
            return;
        }
        if (spot == null || model.getLatitude() <= -9999 || model.getLongitude() <= -9999) {
            mDataBinding.clInterestingSpot.setVisibility(View.INVISIBLE);
            mDataBinding.clNoSpot.setVisibility(View.VISIBLE);
        } else {
            mDataBinding.clInterestingSpot.setVisibility(View.VISIBLE);
            mDataBinding.clNoSpot.setVisibility(View.INVISIBLE);
            drawRoute(spot.getLatitude(), spot.getLongitude(), model.getLatitude(), model.getLongitude());
            mDataBinding.setSpot(spot);
        }
    }

    private void drawRoute(double latitude, double longitude, double userLatitude, double userLongitude) {
        if (map != null && mapLoaded) {
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
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
                if (model.getMySpotTakenLiveData().getValue() == null) {
                    verifyCurrentInterestingSpot(model.getInterestingSpot());
                }
                mDataBinding.setDisableTakeMySpot(locationResult.getLastLocation() == null);
            }
        }, Looper.myLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mapLoaded = true;
            }
        });
    }

    public void enterExitReservedSpotMode(boolean enter) {
        //mDataBinding.btnTakeSpot.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        //mDataBinding.clInterestingSpot.setVisibility(enter ? View.VISIBLE : View.INVISIBLE);
        mDataBinding.tvTitle.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.tvReserved.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.ibAccept.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
        mDataBinding.ibClose.setVisibility(enter ? View.INVISIBLE : View.VISIBLE);
    }


    private void drawTakerRoute(double latitude, double longitude, double takerLatitude, double takerLongitude) {
        if (map != null && mapLoaded) {
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


    private void startBeaconMode() {
        if (checkPrerequisites()) {
            // Sets up to transmit as an AltBeacon-style beacon.  If you wish to transmit as a different
            // type of beacon, simply provide a different parser expression.  To find other parser expressions,
            // for other beacon types, do a Google search for "setBeaconLayout" including the quotes
            mBeaconTransmitter = new BeaconTransmitter(this, new BeaconParser().setBeaconLayout(BeaconsContants.BEACONS_LAYOUT_CONDUCTOR));
            // Transmit a beacon with Identifiers 2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6 1 2
            Beacon beacon = new Beacon.Builder()
                    .setBluetoothName("TakeMySpot")
                    .setId1(TakeMySpotApp.getInstance().getPushToken())
                    .setId2("1")
                    .setId3("2")
                    .setManufacturer(0x0000) // Choose a number of 0x00ff or less as some devices cannot detect beacons with a manufacturer code > 0x00ff
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[]{0l}))
                    .build();
            mBeaconTransmitter.startAdvertising(beacon);
        }
    }

    private boolean checkPrerequisites() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not supported by this device's operating system");
            builder.setMessage("You will not be able to transmit as a Beacon");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //TODO finish();
                }

            });
            builder.show();
            return false;
        }
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not supported by this device");
            builder.setMessage("You will not be able to transmit as a Beacon");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //TODO finish();
                }

            });
            builder.show();
            return false;
        }
        if (!((BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth not enabled");
            builder.setMessage("Please enable Bluetooth and restart this app.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //TODO finish();
                }

            });
            builder.show();
            return false;
        }

        try {
            // Check to see if the getBluetoothLeAdvertiser is available.  If not, this will throw an exception indicating we are not running Android L
            ((BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().getBluetoothLeAdvertiser();
        } catch (Exception e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE advertising unavailable");
            builder.setMessage("Sorry, the operating system on this device does not support Bluetooth LE advertising.  As of July 2014, only the Android L preview OS supports this feature in user-installed apps.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //TODO finish();
                }

            });
            builder.show();
            return false;

        }
        return true;
    }

    private void scanForBeacons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
                return;
            }
        }

        mBeaconScanner = new BeaconScanner(this) {
            @Override
            public void checkSpot(String senderId, final VerifyBeaconListener verifyBeaconListener) {
                model.verifySpot(senderId, new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        JsonObject body = response.body();
                        if (response.isSuccessful() && body != null && body.get("result").getAsBoolean()) {
                            Toast.makeText(getApplicationContext(), "Your spot has been validated! You can now enjoy it!", Toast.LENGTH_SHORT).show();
                            verifyBeaconListener.onBeaconValidated();
                            mBeaconScanner.unbind();
                            mHandler.removeCallbacks(mStopScanRunnable);
                            model.dismissCurrentInterestingLocation();
                        } else {
                            verifyBeaconListener.onBeaconValidationFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Something went wrong validating the spot", Toast.LENGTH_SHORT).show();
                        verifyBeaconListener.onBeaconValidationFailed();
                    }
                });
            }
        };
        mBeaconScanner.scan();

        if (mHandler == null) {
            mHandler = new Handler();
        }

        if (mStopScanRunnable == null) {
            mStopScanRunnable = new Runnable() {

                @Override
                public void run() {
                    mBeaconScanner.unbind();
                }
            };
        }
        mHandler.postDelayed(mStopScanRunnable, 5000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerLocationUpdates();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBeaconTransmitter != null) {
            mBeaconTransmitter.stopAdvertising();
        }
        if (mBeaconScanner != null) {
            mBeaconScanner.unbind();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconScanner.onBeaconServiceConnect();
    }

}

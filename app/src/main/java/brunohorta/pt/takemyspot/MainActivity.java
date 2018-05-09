package brunohorta.pt.takemyspot;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private double latitude;
    private double longitude;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        button = findViewById(R.id.takeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSpot();
            }
        });

        registerLocationUpdates();
    }

    private void registerSpot() {
        SpotWrapper.getInstance().registerSpot(new Spot(TakeMySpotApp.getInstance().getPushToken(), latitude, longitude), new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Toast.makeText(getApplicationContext(), "Your spot is registered", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error one problem occurs", Toast.LENGTH_SHORT).show();
            }
        });
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
                if (locationResult.getLastLocation() == null) {
                    latitude = -9999;
                    longitude = -9999;
                } else {
                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();
                }
                System.out.println(latitude);
                System.out.println(longitude);
            }
        }, Looper.myLooper());
    }
}

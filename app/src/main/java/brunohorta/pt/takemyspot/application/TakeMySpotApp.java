package brunohorta.pt.takemyspot.application;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import com.onesignal.OneSignal;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

import brunohorta.pt.takemyspot.preferences.LocationPreferences;
import brunohorta.pt.takemyspot.preferences.SpotPreferences;

public class TakeMySpotApp extends Application {
    //TODO https://github.com/AltBeacon/altbeacon-transmitter-android
    private static TakeMySpotApp mInstance;
    private String pushToken;
    private LocationPreferences locationPreferences;
    private SpotPreferences spotPreferences;
    BeaconTransmitter mBeaconTransmitter;

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                pushToken = userId;
            }
        });
        locationPreferences = new LocationPreferences(this);
        spotPreferences = new SpotPreferences(this);
        mInstance = this;
        if (checkPrerequisites()) {

            // Sets up to transmit as an AltBeacon-style beacon.  If you wish to transmit as a different
            // type of beacon, simply provide a different parser expression.  To find other parser expressions,
            // for other beacon types, do a Google search for "setBeaconLayout" including the quotes
            mBeaconTransmitter = new BeaconTransmitter(this, new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            // Transmit a beacon with Identifiers 2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6 1 2
            Beacon beacon = new Beacon.Builder()
                    .setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6")
                    .setId2("1")
                    .setId3("2")
                    .setManufacturer(0x0000) // Choose a number of 0x00ff or less as some devices cannot detect beacons with a manufacturer code > 0x00ff
                    .setTxPower(-59)
                    .setDataFields(Arrays.asList(new Long[] {0l}))
                    .build();

            mBeaconTransmitter.startAdvertising(beacon);
        }
    }

    @TargetApi(21)
    private boolean checkPrerequisites() {

        if (android.os.Build.VERSION.SDK_INT < 18) {
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
        if (!((BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
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
        }
        catch (Exception e) {
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

    public String getPushToken() {
        return pushToken;
    }

    public static synchronized TakeMySpotApp getInstance() {
        return mInstance;
    }

    public LocationPreferences getLocationPreferences() {
        return locationPreferences;
    }

    public SpotPreferences getSpotPreferences() {
        return spotPreferences;
    }
}
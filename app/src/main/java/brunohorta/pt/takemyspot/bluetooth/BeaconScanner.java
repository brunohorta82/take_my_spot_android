package brunohorta.pt.takemyspot.bluetooth;
/**
 * Created by Nikolay Vasilev on 11/30/2015.
 */

import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;

import brunohorta.pt.takemyspot.application.TakeMySpotApp;

public abstract class BeaconScanner {

    private static final String TAG = BeaconScanner.class.getSimpleName();
    public static final String TAKE_MY_SPOT_IDENTIFIER_EXTRA = "TakeMySpotIdentifierExtra";

    private BeaconManager mBeaconManager;

    private BeaconConsumer mBeaconConsumer;

    public BeaconScanner(BeaconConsumer beaconConsumer) {
        this.mBeaconConsumer = beaconConsumer;
    }

    public void scan() {
        if (mBeaconManager == null) {
            mBeaconManager = BeaconManager.getInstanceForApplication(TakeMySpotApp.getInstance());

            List<BeaconParser> beaconParsers = mBeaconManager.getBeaconParsers();
            //if (beaconParsers.size() <= 0) {
            beaconParsers.add(new BeaconParser()
                    .setBeaconLayout(BeaconsContants.BEACONS_LAYOUT_CONDUCTOR));
            //}

            mBeaconManager.setForegroundScanPeriod(1000);
        }

        if (!mBeaconManager.isBound(mBeaconConsumer)) {
            Log.v(TAG, "BOUND");
            mBeaconManager.bind(mBeaconConsumer);
        }
    }

    public void unbind() {
        if (mBeaconManager != null && mBeaconManager.isBound(mBeaconConsumer)) {
            Log.v(TAG, "UNBOUND");
            mBeaconManager.unbind(mBeaconConsumer);
        }
    }

    public void onBeaconServiceConnect() {
        Log.v(TAG, "ON BEACON SERVICE CONNECT");
        mBeaconManager.setRangeNotifier(mRangeNotifier);

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region(TAKE_MY_SPOT_IDENTIFIER_EXTRA, Identifier.parse(TakeMySpotApp.getInstance().getSpotPreferences().getSpot().getSenderId()), null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected RangeNotifier mRangeNotifier = new RangeNotifier() {

        private boolean mIsOpened = false;

        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if (beacons.size() > 0) {
                Beacon nearestBeacon = beacons.iterator().next();
                if (nearestBeacon != null && !mIsOpened && nearestBeacon.getDistance() < .01/*5*/) {
                    mIsOpened = true;
                    checkSpot(nearestBeacon.getId1().toString(), new VerifyBeaconListener() {
                        @Override
                        public void onBeaconValidated() {
                            mBeaconManager.unbind(mBeaconConsumer);
                        }

                        @Override
                        public void onBeaconValidationFailed() {
                            mIsOpened = false;
                        }
                    });
                }
            }
        }
    };

    public abstract void checkSpot(String senderId, VerifyBeaconListener verifyBeaconListener);

    public interface VerifyBeaconListener {
        void onBeaconValidated();

        void onBeaconValidationFailed();
    }
}
package brunohorta.pt.takemyspot.application;

import android.app.Application;

import com.onesignal.OneSignal;

import brunohorta.pt.takemyspot.preferences.LocationPreferences;
import brunohorta.pt.takemyspot.preferences.SpotPreferences;

public class TakeMySpotApp extends Application {

    private static TakeMySpotApp mInstance;
    private String pushToken;
    private LocationPreferences locationPreferences;
    private SpotPreferences spotPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.InAppAlert)
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
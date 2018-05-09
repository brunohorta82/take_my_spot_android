package brunohorta.pt.takemyspot;

import android.app.Application;

import com.onesignal.OneSignal;

public class TakeMySpotApp extends Application {
    
    private static TakeMySpotApp mInstance;
    private String pushToken;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
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
    }
    
    public String getPushToken() {
        return pushToken;
    }

    public static synchronized TakeMySpotApp getInstance() {
        return mInstance;
    }

}
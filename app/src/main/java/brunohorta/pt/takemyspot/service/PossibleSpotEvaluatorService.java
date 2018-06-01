package brunohorta.pt.takemyspot.service;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.repository.SpotRepository;

import static brunohorta.pt.takemyspot.preferences.LocationPreferences.LATITUDE;
import static brunohorta.pt.takemyspot.preferences.LocationPreferences.LONGITUDE;

public class PossibleSpotEvaluatorService extends JobIntentService {
    public static String SENDER_ID = "senderId";
    public static String SPOT_ID = "spotId";
    public static String TIMESTAMP = "timestamp";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        double latitude = intent.getDoubleExtra(LATITUDE, -9999);
        double longitude = intent.getDoubleExtra(LONGITUDE, -9999);
        long timestamp = intent.getLongExtra(TIMESTAMP, System.currentTimeMillis());
        String senderId = intent.getStringExtra(SENDER_ID);
        long spotId = intent.getLongExtra(SPOT_ID, 0);
        if (latitude != -9999 && longitude != -9999 && SpotRepository.getInstance().getCurrentInterestingSpot() == null && TakeMySpotApp.getInstance().getPushToken() != null && !TakeMySpotApp.getInstance().getPushToken().equals(senderId)) {
            if (SpotRepository.getInstance().isSpotInteresting(timestamp, latitude, longitude)) {
                SpotRepository.getInstance().setInterestingSpotAvailable(new Spot(System.currentTimeMillis(), spotId, senderId, latitude, longitude));
            }
        }
    }
}

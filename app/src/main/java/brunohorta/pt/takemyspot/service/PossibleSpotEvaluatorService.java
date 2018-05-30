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

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        double latitude = intent.getDoubleExtra(LATITUDE, -9999);
        double longitude = intent.getDoubleExtra(LONGITUDE, -9999);
        String senderId = intent.getStringExtra(SENDER_ID);
        long spotId = intent.getLongExtra(SPOT_ID, 0);
        if (latitude != -9999 && longitude != -9999 && SpotRepository.getInstance().getCurrentInterestingSpot() == null && !TakeMySpotApp.getInstance().getPushToken().equals(senderId)) {
            if (SpotRepository.getInstance().isSpotInteresting(latitude, longitude)) {
                SpotRepository.getInstance().setInterestingSpotAvailable(new Spot(spotId, senderId, latitude, longitude));
                System.out.println("!!!!!!" + latitude + ", " + longitude);
            }
        }
    }
}

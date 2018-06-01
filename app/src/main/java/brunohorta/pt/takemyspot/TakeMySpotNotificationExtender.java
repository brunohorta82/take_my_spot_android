package brunohorta.pt.takemyspot;

import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.MessageType;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.preferences.LocationPreferences;
import brunohorta.pt.takemyspot.repository.SpotRepository;

import static brunohorta.pt.takemyspot.preferences.LocationPreferences.LATITUDE;
import static brunohorta.pt.takemyspot.preferences.LocationPreferences.LONGITUDE;
import static brunohorta.pt.takemyspot.service.PossibleSpotEvaluatorService.SENDER_ID;
import static brunohorta.pt.takemyspot.service.PossibleSpotEvaluatorService.SPOT_ID;
import static brunohorta.pt.takemyspot.service.PossibleSpotEvaluatorService.TIMESTAMP;

public class TakeMySpotNotificationExtender extends NotificationExtenderService {


    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        //System.out.println("notification received");
        JSONObject jsonObject = receivedResult.payload.additionalData;
        try {
            if (jsonObject == null || jsonObject.isNull("type") || jsonObject.getString("type") == null) {//if no message type is present we ignore the notification
                return true;
            }
            if (jsonObject.getString("type").equals(MessageType.NEW_SPOT.name()) && SpotRepository.getInstance().getCurrentInterestingSpot() == null
                    && !jsonObject.getString(SENDER_ID).equals(TakeMySpotApp.getInstance().getPushToken())) {
                //Intent i = new Intent(TakeMySpotApp.getInstance(), PossibleSpotEvaluatorService.class);
                //i.putExtra(LocationPreferences.LATITUDE, jsonObject.getDouble(LocationPreferences.LATITUDE));
                //i.putExtra(LocationPreferences.LONGITUDE, jsonObject.getDouble(LocationPreferences.LONGITUDE));
                //i.putExtra(PossibleSpotEvaluatorService.SENDER_ID, jsonObject.getString(PossibleSpotEvaluatorService.SENDER_ID));
                //i.putExtra(PossibleSpotEvaluatorService.SPOT_ID, jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID));
                //i.putExtra(PossibleSpotEvaluatorService.TIMESTAMP, jsonObject.getLong(PossibleSpotEvaluatorService.TIMESTAMP));
                //PossibleSpotEvaluatorService.enqueueWork(TakeMySpotApp.getInstance(), PossibleSpotEvaluatorService.class, 1000, i);

                double latitude = jsonObject.getDouble(LATITUDE);
                double longitude = jsonObject.getDouble(LONGITUDE);
                long timestamp = jsonObject.getLong(TIMESTAMP);
                String senderId = jsonObject.getString(SENDER_ID);
                long spotId = jsonObject.getLong(SPOT_ID);
                if (SpotRepository.getInstance().getCurrentInterestingSpot() == null && TakeMySpotApp.getInstance().getPushToken() != null && !TakeMySpotApp.getInstance().getPushToken().equals(senderId)) {
                    if (SpotRepository.getInstance().isSpotInteresting(timestamp, latitude, longitude)) {
                        SpotRepository.getInstance().setInterestingSpotAvailable(new Spot(System.currentTimeMillis(), spotId, senderId, latitude, longitude));
                        return false;
                    }
                }
                return true;
            } else if (jsonObject.getString("type").equals(MessageType.TIMEOUT.name())) {
                if (SpotRepository.getInstance().getCurrentInterestingSpot() != null && Spot.isSame(SpotRepository.getInstance().getCurrentInterestingSpot(), jsonObject.getLong(SPOT_ID))) {
                    SpotRepository.getInstance().setInterestingSpotAvailable(null);
                }
            } else if (jsonObject.getString("type").equals(MessageType.NAVIGATION.name())) {
                Spot value = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();
                if (value != null && Spot.isSame(value, jsonObject.getLong(SPOT_ID))) {
                    SpotRepository.getInstance().markMySpotTakenTakerLocation(
                            jsonObject.getDouble(LocationPreferences.LATITUDE),
                            jsonObject.getDouble(LocationPreferences.LONGITUDE));
                }
                return true;
            } else if (jsonObject.getString("type").equals(MessageType.RESERVED.name())) {
                Spot spot = new Spot(System.currentTimeMillis(), jsonObject.getLong(SPOT_ID),
                        //jsonObject.getString(PossibleSpotEvaluatorService.SENDER_ID),
                        TakeMySpotApp.getInstance().getPushToken(),
                        jsonObject.getDouble(LocationPreferences.LATITUDE),
                        jsonObject.getDouble(LocationPreferences.LONGITUDE));
                SpotRepository.getInstance().updateMySpotAsTaken(spot);
            } else if (jsonObject.getString("type").equals(MessageType.TIMEOUT_RESERVED.name())) {
                Spot value = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();
                if (value != null && Spot.isSame(value, jsonObject.getLong(SPOT_ID))) {
                    SpotRepository.getInstance().updateMySpotAsTaken(null);
                }
                return true;
            } else if (jsonObject.getString("type").equals(MessageType.VALIDATION.name())) {
                Spot value = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();
                if (value != null && Spot.isSame(value, jsonObject.getLong(SPOT_ID))) {
                    Spot value1 = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();
                    if (value1 != null) {
                        value1.setValidated(true);
                    }
                    SpotRepository.getInstance().updateMySpotAsTaken(value1);
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("OneSignalExample", "Notification displayed");
        return false;
    }
}
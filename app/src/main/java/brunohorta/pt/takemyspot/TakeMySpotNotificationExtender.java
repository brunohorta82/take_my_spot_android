package brunohorta.pt.takemyspot;

import android.content.Intent;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import brunohorta.pt.takemyspot.application.TakeMySpotApp;
import brunohorta.pt.takemyspot.entity.MessageType;
import brunohorta.pt.takemyspot.entity.Spot;
import brunohorta.pt.takemyspot.preferences.LocationPreferences;
import brunohorta.pt.takemyspot.repository.SpotRepository;
import brunohorta.pt.takemyspot.service.PossibleSpotEvaluatorService;

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
                    && !jsonObject.getString(PossibleSpotEvaluatorService.SENDER_ID).equals(TakeMySpotApp.getInstance().getPushToken())) {
                Intent i = new Intent(TakeMySpotApp.getInstance(), PossibleSpotEvaluatorService.class);
                i.putExtra(LocationPreferences.LATITUDE, jsonObject.getDouble(LocationPreferences.LATITUDE));
                i.putExtra(LocationPreferences.LONGITUDE, jsonObject.getDouble(LocationPreferences.LONGITUDE));
                i.putExtra(PossibleSpotEvaluatorService.SENDER_ID, jsonObject.getString(PossibleSpotEvaluatorService.SENDER_ID));
                i.putExtra(PossibleSpotEvaluatorService.SPOT_ID, jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID));
                PossibleSpotEvaluatorService.enqueueWork(TakeMySpotApp.getInstance(), PossibleSpotEvaluatorService.class, 1000, i);
                return false;
            } else if (jsonObject.getString("type").equals(MessageType.TIMEOUT.name())) {
                 if (SpotRepository.getInstance().getCurrentInterestingSpot() != null && Spot.isSame(SpotRepository.getInstance().getCurrentInterestingSpot(), jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID))) {
                    SpotRepository.getInstance().setInterestingSpotAvailable(null);
                }
            } else if (jsonObject.getString("type").equals(MessageType.NAVIGATION.name())) {
                Spot value = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();

                if (value != null && Spot.isSame(value, jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID))) {
                    SpotRepository.getInstance().markMySpotTakenTakerLocation(
                            jsonObject.getDouble(LocationPreferences.LATITUDE),
                            jsonObject.getDouble(LocationPreferences.LONGITUDE));
                }
            } else if (jsonObject.getString("type").equals(MessageType.RESERVED.name())) {
                Spot spot = new Spot(
                        jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID),
                        //jsonObject.getString(PossibleSpotEvaluatorService.SENDER_ID),
                        TakeMySpotApp.getInstance().getPushToken(),
                        jsonObject.getDouble(LocationPreferences.LATITUDE),
                        jsonObject.getDouble(LocationPreferences.LONGITUDE));
                SpotRepository.getInstance().updateMySpotAsTaken(spot);
            }else if (jsonObject.getString("type").equals(MessageType.TIMEOUT_RESERVED.name())) {
                Spot value = SpotRepository.getInstance().getMySpotTakenLiveData().getValue();
                if (value != null && Spot.isSame(value, jsonObject.getLong(PossibleSpotEvaluatorService.SPOT_ID))) {
                    SpotRepository.getInstance().updateMySpotAsTaken(null);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
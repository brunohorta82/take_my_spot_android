package brunohorta.pt.takemyspot;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationExtenderBareBonesExample extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        System.out.println("notification received");
        JSONObject jsonObject = receivedResult.payload.additionalData;
        try {
            if (TakeMySpotApp.getInstance().getPushToken().equals(jsonObject.getString("pushToken")))

                // Return true to stop the notification from displaying.
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
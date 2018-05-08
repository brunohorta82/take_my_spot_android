package brunohorta.pt.takemyspot;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONObject;

public class NotificationExtenderBareBonesExample extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.
        System.out.println("notification received");
        JSONObject jsonObject = receivedResult.payload.additionalData;

        // Return true to stop the notification from displaying.
        return false;
    }
}
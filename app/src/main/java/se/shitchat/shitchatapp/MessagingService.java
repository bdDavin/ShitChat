package se.shitchat.shitchatapp;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("INCOMING", "INCOMING");
        Map<String, String> data = remoteMessage.getData();
        String message = remoteMessage.getNotification().getBody();
        String groupId = data.get("groupId");
        NewMessageNotification.notify(this, message, groupId);
    }
}

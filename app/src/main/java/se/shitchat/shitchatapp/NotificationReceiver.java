package se.shitchat.shitchatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationReceiver extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    private void showNotification(String title, String body) {
        NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "se.shitchat.shitchatapp";

        NotificationChannel notiChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
                NotificationManager.IMPORTANCE_HIGH);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}

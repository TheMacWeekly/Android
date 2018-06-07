package hu.ait.macweekly.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import hu.ait.macweekly.MainActivity;
import hu.ait.macweekly.R;

/**
 * Created by Sammy F on 6/7/2018.
 */

public class MessageReceiver extends FirebaseMessagingService {
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 5555;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        final String title = remoteMessage.getData().get("title");
        final String body = remoteMessage.getData().get("body");

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {

        Intent i = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                    i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(body)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.app_icon_128_transparent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}

package id.sch.smkn1batukliang.inventory.utili;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import id.sch.smkn1batukliang.inventory.R;
import id.sch.smkn1batukliang.inventory.SplashScreenActivity;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class InventoryMessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String SERVER_KEY = "AAAAzGSj4nY:APA91bGZKfUlyv3EuN_iCAlbv_VjVSSwkf00GJHOsgGHDwmF-8-vZUz7fIzLtaeAqPPwxwtN8pD8HDPlSdku5RLuwUYpea6bGNyujkYYRlrWVND-YPWEjI1L4Igp-_aXpM0DZd2zH4BJ";

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_1";
    private static final CharSequence CHANNEL_NAME = "inventory_smk_n_1_batukliang";
    private static final String TAG = "InventoryMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> map = remoteMessage.getData();
            String title = map.get("title");
            String message = map.get("message");

            sendNotification(title, message);

        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_NAME.toString());
            builder.setChannelId(CHANNEL_ID);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = builder.build();
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

    }
}

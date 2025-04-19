package com.lena.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lena.android.App;
import com.lena.android.AppHomeActivity;
import com.lena.android.R;
import com.lena.android.utils.Logger;
import com.lena.android.utils.TimeUtil;
import com.lena.android.utils.VerifyUtil;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    public final static String CHANNEL_ID = "com.lena.android.notification.message1";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Logger.debug(TAG, "receive");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (null != notification) {
            Map<String, String> data = remoteMessage.getData();
            if (!data.isEmpty()) {
                for (String s : data.keySet()) {
                    Logger.debug(TAG, s + ": " + String.valueOf(data.get(s)));
                }
            }
            sendNotification(notification.getTitle(), notification.getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Logger.debug(TAG, "Refreshed token: " + token);
        // TODO: 通知自己的服务器
    }

    private void sendNotification(String title, String messageBody) {
        if (VerifyUtil.aNotEmptyString(messageBody) && VerifyUtil.aNotEmptyString(title)) {
            final Intent intent = new Intent(this, AppHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.drawable.app_ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setWhen(TimeUtil.getCurrentTimeMillis())
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(++App.app.notificationIndex, notificationBuilder.build());
        }
    }
}
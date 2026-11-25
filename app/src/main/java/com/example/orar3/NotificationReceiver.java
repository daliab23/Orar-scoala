package com.example.orar3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String subject = intent.getStringExtra("subject");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String channelId = intent.getStringExtra("channelId");

        if (channelId == null || channelId.isEmpty()) {
            channelId = "test_channel";
        }

        if (title == null || title.isEmpty()) {
            title = "Notificare Curs";
        }

        if (message == null || message.isEmpty()) {
            message = "Nu uita că ai test la " + (subject != null ? subject : "curs") + " 📚";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        int notificationID = intent.getIntExtra("requestCode", 0);
        manager.notify(notificationID, builder.build());
    }
}

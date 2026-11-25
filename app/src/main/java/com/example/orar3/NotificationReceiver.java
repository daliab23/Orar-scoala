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


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "test_channel")
                .setSmallIcon(R.drawable.ic_stat_name) // pune o iconiță
                .setContentTitle(title)     //Ai test maine | Ai test azi
                .setContentText("Nu uita că ai test la " + subject + " 📚")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        //Verifica permisiunea pentru a trimite notificari
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int notificationID = intent.getIntExtra("requestCode",0);
        manager.notify(notificationID, builder.build());
    }
}

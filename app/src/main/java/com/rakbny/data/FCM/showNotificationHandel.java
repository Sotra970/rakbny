package com.rakbny.data.FCM;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.rakbny.Activity.FollowerActivity;
import com.rakbny.R;
import com.rakbny.data.app.Config;

import java.util.Map;

/**
 * Created by Sotraa on 6/15/2016.
 */
public class showNotificationHandel {
    Context context ;
    static int ID =0 ;


    public showNotificationHandel(Context context, Map<String, String> currentElemnt) {
//        this.context = context ;
//        sendFeedNotification(currentElemnt);


    }
  public showNotificationHandel(Context context ,String distance) {
      this.context = context ;
      send_bus_distance_Notification(distance);


    }

    public showNotificationHandel(Context context) {
        this.context = context;
        send_child_state_notification();
    }

    private void send_bus_distance_Notification(String distance ){
        Intent intent = new Intent (context , FollowerActivity.class);
      //  intent.setAction(Config.UPDATE_BUS_LOCATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from","notification");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Bus distance")
                .setAutoCancel(true)
                .setContentText(distance)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority( NotificationCompat.PRIORITY_MAX);
        final Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID++ /* ID of notification */, notification);

    }
    private void send_child_state_notification( ){
        Intent intent = new Intent (context , FollowerActivity.class);
        //  intent.setAction(Config.UPDATE_BUS_LOCATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from","notification");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Child  attendance ")
                .setAutoCancel(true)
                .setContentText("Your child have rode the bus")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority( NotificationCompat.PRIORITY_MAX);
        final Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID++ /* ID of notification */, notification);

    }


}

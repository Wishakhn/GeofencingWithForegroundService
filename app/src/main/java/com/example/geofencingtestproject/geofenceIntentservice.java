package com.example.geofencingtestproject;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class geofenceIntentservice extends IntentService {
    private static final int JOB_ID = 573;

    private static final String TAG = "GeofenceTransitionsIS";

    private static final String CHANNEL_ID = "channel_01";
    String geofenceDetails = "";

    public geofenceIntentservice() {
        super("geofence-service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessage.getErrorString(this,
                    geofencingEvent.getErrorCode());
            System.out.println("Error in geofence is ::" + errorMessage);
            return;
        }
        int geotransition = geofencingEvent.getGeofenceTransition();
       /* if (geotransition == Geofence.GEOFENCE_TRANSITION_ENTER || geotransition == Geofence.GEOFENCE_TRANSITION_EXIT )
        {
            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
            geofenceDetails = getGeofenceDetails(geotransition, geofenceList);
            System.out.println("GeoFence Details ::" + geofenceDetails);
        }*/
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        geofenceDetails = getGeofenceDetails(geotransition, geofenceList);
        System.out.println("GeoFence Details ::" + geofenceDetails);
        sendNotification(geofenceDetails);
    }

    private String getGeofenceDetails(int geotransition, List<Geofence> geofenceList) {
        List<String> trigger = new ArrayList<>();

        for (Geofence geofence : geofenceList) {
            trigger.add(geofence.getRequestId());
        }
        String userStatus = null;
        if (geotransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            userStatus = "User is ENTERING";

        }
        if (geotransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            userStatus = "User is EXITING";
        }
        if (geotransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            userStatus = "User is DWELLING";
        }
        sendNotification(userStatus);
        return userStatus + TextUtils.join(", ", trigger);
    }

    private void sendNotification(String notificationDetails) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher_foreground))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText("Test Geofence as started working")
                .setContentIntent(notificationPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        builder.setAutoCancel(true);
        mNotificationManager.notify(0, builder.build());
    }
}

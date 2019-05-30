package com.gpig.a.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.gpig.a.PollServer;
import com.gpig.a.R;

public final class NotificationUtils {

    public static void notify(Context context, String title, String text) {
        Intent intent = new Intent(context, PollServer.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Notification.Builder nBuilder = new Notification.Builder(context);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);
        nBuilder.setContentIntent(pIntent);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher, Notification.BADGE_ICON_SMALL);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, nBuilder.build());
    }
}

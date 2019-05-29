package com.gpig.a.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.gpig.a.R;

public final class NotificationUtils {

    public static void notify(Context context, String title, String text) {
        Notification.Builder nBuilder = new Notification.Builder(context);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher, Notification.BADGE_ICON_SMALL);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, nBuilder.build());
    }
}

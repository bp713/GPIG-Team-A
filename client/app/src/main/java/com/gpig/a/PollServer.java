package com.gpig.a;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gpig.a.settings.Settings;
import com.gpig.a.utils.NotificationUtils;
import com.gpig.a.utils.ServerUtils;
import com.gpig.a.utils.StatusUtils;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public class PollServer extends BroadcastReceiver {
    public static final String TAG = "PollServer";
    public static boolean areUpdatesAvailable = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {//TODO notifications fail to show
        Log.d(TAG, "onReceive: ");
        if(ServerUtils.hasUpdate(context)){
            areUpdatesAvailable = true;
            NotificationUtils.notify(context, "Updates Available", "New updates are available, sign into the app for more details");
        }
    }

    public void setAlarm(Context context)
    {
        Log.i(TAG, "setAlarm: ");
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, PollServer.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, PollServer.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}

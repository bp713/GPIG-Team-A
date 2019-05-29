package com.gpig.a;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.gpig.a.settings.Settings;
import com.gpig.a.utils.NotificationUtils;
import com.gpig.a.utils.ServerUtils;
import com.gpig.a.utils.StatusUtils;

import java.io.FileNotFoundException;

public class PollServer extends BroadcastReceiver {
    public static final String TAG = "PollServer";
    public boolean areUpdatesAvailable = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {//TODO notifications fail to show
        Log.i(TAG, "onReceive: ");
        if(Settings.SessionKey.equals("") || Integer.parseInt(Settings.SessionKey.split(",")[1]) > System.currentTimeMillis()/1000){
            // if there is no session key or it has expired then cancel the alarm
            NotificationUtils.notify(context, "Not Receiving Updates", "Please login to check for new updates");
            this.cancelAlarm(context);
        }
        //TODO check for network
//        if(!StatusUtils.isNetworkAvailable()){
//            // no network so cant poll
//            return;
//        }
        String updates = ServerUtils.getFromServer("route/updates?session_key=" + Settings.SessionKey);
        if (updates.contains("True")) {//TODO handle updates
            areUpdatesAvailable = true;
            NotificationUtils.notify(context, "Updates Available", "New updates are available sign into the app for more detail");
            // Put here YOUR code.
            Toast.makeText(context, updates, Toast.LENGTH_LONG).show();
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

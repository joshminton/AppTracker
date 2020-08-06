package com.jminton.apptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class RestartReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Log.d("HERE", "HERE");

        Toast.makeText(context, "ON RESTART", Toast.LENGTH_LONG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,TrackingService.class));
        } else {
            context.startService(new Intent(context, TrackingService.class));
        }
    }

}

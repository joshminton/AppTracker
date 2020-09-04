package com.jminton.apptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

//https://stackoverflow.com/a/16954799/3032936, and a bit of https://stackoverflow.com/a/6440759/3032936

public class RestartReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,TrackingService.class));
        } else {
            context.startService(new Intent(context, TrackingService.class));
        }
    }

}

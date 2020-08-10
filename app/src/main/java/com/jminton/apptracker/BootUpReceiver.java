package com.jminton.apptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    //https://stackoverflow.com/a/20920004/3032936
    public void onReceive(Context context, Intent intent) {
//        /****** For Start Activity *****/
//        Intent i = new Intent(context, MyActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);

//        Toast.makeText(context, "ON BOOT", Toast.LENGTH_LONG);

        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        Log.d(context.getApplicationContext().getPackageName(), " gazorpa ");
        context.getApplicationContext().registerReceiver(new PowerReceiver(), ifilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,TrackingService.class));
        } else {
            context.startService(new Intent(context, TrackingService.class));
        }

    }

}

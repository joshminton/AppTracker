package com.jminton.apptracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

//https://stackoverflow.com/a/20392715/3032936
public class PowerReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            //Handle power connected
            Log.d("Power", "connected");
        } else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED){
            //Handle power disconnected
            Log.d("Power", "disconnected");
        }
    }
}

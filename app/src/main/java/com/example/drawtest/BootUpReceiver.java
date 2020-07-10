package com.example.drawtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{

    @Override
    //https://stackoverflow.com/a/20920004/3032936
    public void onReceive(Context context, Intent intent) {
//        /****** For Start Activity *****/
//        Intent i = new Intent(context, MyActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);

        /***** For start Service  ****/
        Intent startServiceIntent = new Intent(context, TrackingService.class);
        context.startService(startServiceIntent);
    }

}

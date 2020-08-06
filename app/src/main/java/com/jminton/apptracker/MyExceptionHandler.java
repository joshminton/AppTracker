package com.jminton.apptracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Service service;

    public MyExceptionHandler(Service s) {
        service = s;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Log.d("HEY", "----------------------");


        Intent restartIntent = new Intent(service, RestartReceiver.class);

        restartIntent.putExtra("crash", true);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(service.getApplicationContext(), 0, restartIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager mgr = (AlarmManager) service.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            service.getApplicationContext().startForegroundService(new Intent(service.getApplicationContext(), TrackingService.class));
//        } else {
//            service.getApplicationContext().startService(new Intent(service.getApplicationContext(), TrackingService.class));
//        }


        Log.d("HEY", "----------------------");

        System.exit(1);
    }
}
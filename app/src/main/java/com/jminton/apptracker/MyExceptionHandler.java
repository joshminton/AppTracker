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
    public void uncaughtException(Thread thread, Throwable throwable) {
//        Intent intent = getBaseContext().getPackageManager()
//                .getLaunchIntentForPackage(getBaseContext().getPackageName() );
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }
}

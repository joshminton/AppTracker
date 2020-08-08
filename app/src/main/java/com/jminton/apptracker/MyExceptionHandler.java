package com.jminton.apptracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Context context;

    public MyExceptionHandler(Context context) {
        context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
//        Intent intent = getBaseContext().getPackageManager()
//                .getLaunchIntentForPackage(getBaseContext().getPackageName() );
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);

        Intent intent = new Intent(context, MainActivity.class);

        Log.d("Catch!", "catc");

        FirebaseCrashlytics.getInstance().sendUnsentReports();

        context.startActivity(intent);

        Process.killProcess(Process.myPid());
        System.exit(0);

    }
}

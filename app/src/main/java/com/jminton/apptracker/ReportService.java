package com.jminton.apptracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ReportService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.err.println("ReportService refresh");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
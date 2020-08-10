package com.jminton.apptracker;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class RestartService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

//        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
        Log.d("RestartService", "gazorpa");
        if(!isMyServiceRunning(TrackingService.class)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(new Intent(getApplicationContext(),TrackingService.class));
            } else {
                getApplicationContext().startService(new Intent(getApplicationContext(), TrackingService.class));
            }
        }
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
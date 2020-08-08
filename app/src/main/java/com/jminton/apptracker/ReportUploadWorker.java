package com.jminton.apptracker;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReportUploadWorker extends Worker {
    public ReportUploadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        FirebaseCrashlytics.getInstance().sendUnsentReports();

        Log.d("Worker running", "Worker running.");

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
}

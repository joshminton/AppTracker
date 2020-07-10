package com.example.drawtest;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//https://gist.github.com/MaTriXy/9f291bccd8123a5ae8e6cb9e21f627ff
//https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
//https://blog.usejournal.com/building-an-app-usage-tracker-in-android-fe79e959ab26
//https://www.tutorialspoint.com/how-to-manage-startactivityforresult-on-android
//https://inducesmile.com/android/android-list-installed-apps-in-device-programmatically/

public class MainActivity extends FragmentActivity {

    Intent svc;
    TrackingService trackingService;
    boolean bound = false;
    RecyclerView lstApps;
    RecyclerView.LayoutManager layoutManager;
    AppsAdapter lstAppsAdapter;
    HashMap<String, TrackedApp> apps;

    private TrackedApp db;

    AppOpsManager appOps;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;

    private int startHour = 22;
    private int startMinute = 25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        db = Room.databaseBuilder(getApplicationContext(), Favourites.class, "favourites").allowMainThreadQueries().build();

        appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if(mode != AppOpsManager.MODE_ALLOWED){
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        }

        lstApps = (RecyclerView) findViewById(R.id.lstApps);
        layoutManager = new LinearLayoutManager(this);
        lstApps.setLayoutManager(layoutManager);

        apps = new HashMap<>();

        lstAppsAdapter = new AppsAdapter(apps);
        lstApps.setAdapter(lstAppsAdapter);

        svc = new Intent(this, TrackingService.class);
        if(!bound) {
            startService(svc);
            bindService(svc, connection, Context.BIND_AUTO_CREATE);
        }

//        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMyServiceRunning(TrackingService.class)){
            svc = new Intent(this, TrackingService.class);
            bindService(svc, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bound){
            unbindService(connection);
        }
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) { //if it is MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS...
//            if (resultCode == RESULT_OK) {
                if(appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED){
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
//            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickStartStop(View view){
        svc = new Intent(this, TrackingService.class);
        if(!bound) {
            startService(svc);
            bindService(svc, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onClickRefresh(View view){
        if(trackingService == null){
            Log.d("Uh oh", "It's null!");
        } else {
            trackingService.refreshUsageStats();
            apps.clear();
            apps.putAll(trackingService.getTrackedAppsData());
            Log.d("Length", "" + apps.size());
            Objects.requireNonNull(lstApps.getAdapter()).notifyDataSetChanged();
        }
    }


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            trackingService = binder.getService();
            bound = true;
            trackingService.refreshUsageStats();
            apps.clear();
            apps.putAll(trackingService.getTrackedAppsData());
            Log.d("Length", "" + apps.size());
            Objects.requireNonNull(lstApps.getAdapter()).notifyDataSetChanged();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };



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
package com.jminton.apptracker;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.adriangl.overlayhelper.OverlayHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

//https://gist.github.com/MaTriXy/9f291bccd8123a5ae8e6cb9e21f627ff
//https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
//https://blog.usejournal.com/building-an-app-usage-tracker-in-android-fe79e959ab26
//https://www.tutorialspoint.com/how-to-manage-startactivityforresult-on-android
//https://inducesmile.com/android/android-list-installed-apps-in-device-programmatically/
//https://github.com/adriangl/OverlayHelper/blob/master/app/src/main/java/com/adriangl/overlayhelperexample/MainActivity.java

public class MainActivity extends AppCompatActivity implements  BottomNavigationView.OnNavigationItemSelectedListener {

    Intent svc;
    TrackingService trackingService;
    boolean bound = false;
    RecyclerView lstApps;
    RecyclerView.LayoutManager layoutManager;
    AppsAdapter lstAppsAdapter;

    OverlayHelper overlayHelper;

    private boolean drawAccess = false;
    private boolean usageAccess = false;

    private TrackedApp db;

    AppOpsManager appOps;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;

    private int startHour = 22;
    private int startMinute = 25;

    HomeFragment homeFrag = new HomeFragment();
    AppsFragment appsFrag = new AppsFragment();
    LimitsFragment limitsFrag = new LimitsFragment();

    FragmentManager fm = getSupportFragmentManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissions_needed);


//        db = Room.databaseBuilder(getApplicationContext(), Favourites.class, "favourites").allowMainThreadQueries().build();

        appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);


        overlayHelper = new OverlayHelper(this.getApplicationContext(), new OverlayHelper.OverlayPermissionChangedListener() {
            @Override public void onOverlayPermissionCancelled() {
                Toast.makeText(MainActivity.this, "Draw overlay permissions request cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override public void onOverlayPermissionGranted() {
                setDrawPositive();
                if(usageAccess){
                    advanceWithService();
                }
            }

            @Override public void onOverlayPermissionDenied() {
                Toast.makeText(MainActivity.this, "Draw overlay permissions request denied", Toast.LENGTH_SHORT).show();
            }
        });

        overlayHelper.startWatching();

        checkPermissions();

//        finish();
    }

    private void advanceWithService(){

        setContentView(R.layout.activity_main);

        fm.beginTransaction().add(R.id.frag_frame, homeFrag).commit();

        ((BottomNavigationView)findViewById(R.id.bottomNav)).setOnNavigationItemSelectedListener(this);

        svc = new Intent(this, TrackingService.class);
        if(!bound) {
            startForegroundService(svc);
            bindService(svc, connection, Context.BIND_AUTO_CREATE);
        }

        if(trackingService != null){
            trackingService.saveAppsAverageUsageLastTwoWeeks();
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction fT = fm.beginTransaction();
        switch (item.getItemId()) {
            case R.id.home:
                fT.replace(R.id.frag_frame, homeFrag);
                break;
            case R.id.settings:
                fT.replace(R.id.frag_frame, limitsFrag);
                break;
            case R.id.apps:
                fT.replace(R.id.frag_frame, appsFrag);
                break;
        }
        fT.commit();
        return false;
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

    private void checkPermissions(){
        if(overlayHelper.canDrawOverlays()){
            drawAccess = true;
            setDrawPositive();
        }
        if(appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED){
            usageAccess = true;
            setUsagePositive();
        }
        if(usageAccess & drawAccess){
            advanceWithService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) { //if it is MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS...
//            if (resultCode == RESULT_OK) {
                if(appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED){
                    setUsagePositive();
                    usageAccess = true;
                    if(drawAccess){
                        advanceWithService();
                    }
                }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            overlayHelper.onRequestDrawOverlaysPermissionResult(requestCode);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickStartStop(View view){
        svc = new Intent(this, TrackingService.class);
        if(!bound) {
            startForegroundService(svc);
            bindService(svc, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            trackingService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    //https://stackoverflow.com/questions/43513919/android-alert-dialog-with-one-two-and-three-buttons
    public void onClickUsagePermission(View view){
        if(!usageAccess){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Request app usage permission?");
            builder.setMessage("You have to give permission to view device usage statistics for this app to work. Only the amount of time spent in each app will be accessed. No personal information will be exposed.");

            // add the buttons
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
                }
            });
            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void onClickDrawPermission(View view){
        if(!overlayHelper.canDrawOverlays()){
            overlayHelper.requestDrawOverlaysPermission(
                    MainActivity.this,
                    "Request draw overlays permission?",
                    "You have to enable the draw overlays permission for this app to work",
                    "Enable",
                    "Cancel");
        }
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

    private void setUsagePositive(){
        ((CardView) findViewById(R.id.usageAccessCard)).setCardBackgroundColor(getColor(R.color.success));
        findViewById(R.id.txtUsageIcon).setBackground(getDrawable(R.drawable.ic_baseline_check_circle_outline_24));

    }

    private void setDrawPositive(){
        ((CardView) findViewById(R.id.drawAccessCard)).setCardBackgroundColor(getColor(R.color.success));
        findViewById(R.id.txtDrawIcon).setBackground(getDrawable(R.drawable.ic_baseline_check_circle_outline_24));
    }

    public TrackingService getTrackingService(){
        return trackingService;
    }


}
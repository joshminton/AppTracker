package com.jminton.apptracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rvalerio.fgchecker.AppChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//https://stackoverflow.com/questions/55045005/draw-overlay-underneath-navigation-bar
//https://gist.github.com/MaTriXy/9f291bccd8123a5ae8e6cb9e21f627ff
//https://proandroiddev.com/bound-and-foreground-services-in-android-a-step-by-step-guide-5f8362f4ae20

public class TrackingService extends Service {

    private View topLeftView;

    private int height;
    private int width;

    private View overlay;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;

    private android.app.Notification notification;
    private Notification.Builder mBuilder;

    private int startHour = 05;
    private int startMinute = 30;

    private int dailyQuotaMinutes = 30;

    private long interval = 2500;

    HashMap<String, TrackedApp> apps;
    private TrackedAppDatabase db;
    private SharedPreferences sharedPref;
    ArrayList<String> trackedAppCodes;

    private int colourFilterColour;

    private int heavyUseInterval = 5;

    private int LAYOUT_FLAG;

    private boolean visible;

    private AppChecker appChecker = new AppChecker();
    private String currentApp;

    private String lastSavedDate;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        TrackingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

//        db = Room.databaseBuilder(getApplicationContext(), TrackedAppDatabase.class, "tracked-apps").allowMainThreadQueries().build();
        sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        trackedAppCodes = getTrackedAppsFromPrefs();
        loadSavedQuota();

        String NOTIFICATION_CHANNEL_ID = "new_chan2";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Overlay Channel", NotificationManager.IMPORTANCE_LOW);
        chan.setSound(null, null);

        NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, TrackingService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        //https://stackoverflow.com/a/15538209/3032936

        mBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);

        notification =  mBuilder
                .setContentTitle("Tracking your usage.")
//                .setContentText("Currently used " + (int) (quotaPercentageUsed() * 100) + "%")
                .setSmallIcon(R.drawable.ic_baseline_phone_android_24)
                .setContentIntent(pendingIntent)
                .setTicker("Oi oi!")
                .setColorized(true)
                .setOnlyAlertOnce(true)
                .setColor(getResources().getColor(R.color.colorAccent))
                .build();
        startForeground(1, notification);

        visible = true;

        apps = new HashMap<>();

        importAppsList();

        refreshUsageStats();

        setAverageUsageLastWeek();

        lastSavedDate = sharedPref.getString("dateLastSaved", "never");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date todayDate = Calendar.getInstance().getTime();
        String todayString = formatter.format(todayDate);

        Log.d("Last saved date", lastSavedDate + "");

        if(!lastSavedDate.equals(todayString)){
            saveAppsAverageUsageLastTwoWeeks();
        }

        if(sharedPref.getBoolean("doneSetup", false)){
            advanceService();
        }
    }

    public void advanceService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        initOverlay();
        new Handler().post(new tracking());
        updateOverlay("none");
        show(overlay.findViewById(R.id.innerGlow), 1f);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Log.d(this.getPackageName(), "hey");

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        if (intent.getBooleanExtra("crash", false)) {
            Toast.makeText(this, "App restarted after crash", Toast.LENGTH_SHORT).show();

            Log.d("Restart after", "CRASSSSSHHHHHHHHHHHHHHHHHHH");
        }

//        // If we get killed, after returning from here, restart
//        return START_STICKY;

        return android.app.Service.START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay != null) {
            wm.removeView(overlay);
            overlay = null;
        }
//
//        Intent restartIntent = new Intent(this, RestartReceiver.class);
//        this.sendBroadcast(restartIntent);

        Log.d("HEY", "----------------------");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        initOverlay();

        updateOverlay("none");

        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//        }
    }

    private void initOverlay(){

        if(overlay != null){
            wm.removeView(overlay);
        }

        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        height = size.y;
        width = size.x;

        overlay = LayoutInflater.from(this).inflate(R.layout.glow, null);

        View innerGlow = overlay.findViewById(R.id.innerGlow);
        View outerGlow = overlay.findViewById(R.id.outerGlow);
        View fadingEdge = overlay.findViewById(R.id.fadingEdge);

        innerGlow.getLayoutParams().width = width;
        outerGlow.getLayoutParams().width = width;
        innerGlow.getLayoutParams().height = height;
        outerGlow.getLayoutParams().height = height;
        fadingEdge.getLayoutParams().width = width;
        fadingEdge.getLayoutParams().height = height;
        show(innerGlow, 1f);
        hide(outerGlow);

        WindowManager.LayoutParams params = new LayoutParams(width,
                height,
                LAYOUT_FLAG,
                LayoutParams.FLAG_NOT_FOCUSABLE
                        | LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | LayoutParams.FLAG_NOT_TOUCHABLE
                        | LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        wm.addView(overlay, params);
    }



    public void toggle(){
        if(visible){
            overlay.setAlpha(0);
            visible = false;
        } else {
            overlay.setAlpha(1);
            visible = true;
        }
    }

    //https://developer.android.com/training/animation/reveal-or-hide-view
    public void hide(){
        overlay.setAlpha(1f);
        overlay.animate()
                .alpha(0f)
                .setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        overlay.setVisibility(View.GONE);
                    }
                });
        visible = false;
    }

    //https://developer.android.com/training/animation/reveal-or-hide-view
    public void show(){
//        overlay.setAlpha(0f);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate()
                .alpha(1f)
                .setDuration(400)
                .setListener(null);


//        overlay.setAlpha(1);
        visible = true;
    }


    public void importAppsList(){
        PackageManager pm = getPackageManager();
        for(ApplicationInfo a : pm.getInstalledApplications(0)){
            if(pm.getLaunchIntentForPackage(a.packageName) != null) {
                apps.put(a.packageName, new TrackedApp(a.loadLabel(pm).toString(), a.packageName, a.loadIcon(pm), 0));
                if(trackedAppCodes.contains(a.packageName)){
                    apps.get(a.packageName).setTracked(true);
                }
            }
        }
    }

    public Map<String, AppUsageInfo> getUsageInfoThisDay(){
        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendar startCal;
        if((cal.get(Calendar.HOUR_OF_DAY) < startHour) || ((cal.get(Calendar.HOUR_OF_DAY) == startHour) && (cal.get(Calendar.MINUTE) < startMinute))){
            cal.add(Calendar.DATE, -1);
            startCal = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    startHour,
                    startMinute,
                    0);
        } else {

            startCal = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    startHour,
                    startMinute,
                    0);
        }

        return queryUsageStatistics(this, startCal.getTimeInMillis(), System.currentTimeMillis());
    }

    public void refreshUsageStats(){

        Log.d("Refreshing!", " ");

        Map<String, AppUsageInfo> usageStatsMap = getUsageInfoThisDay();

        for(TrackedApp a : apps.values()){
            a.setUsageToday(0);
            if(usageStatsMap.get(a.getPackageName()) != null){
                a.setUsageToday(usageStatsMap.get(a.getPackageName()).getTimeInForeground());
            }
        }

        setAverageUsageLastWeek();
    }

    public void runTracking(final Context context){
        //https://stackoverflow.com/questions/3873659/android-how-can-i-get-the-current-foreground-activity-from-a-service/27642535
        final Handler handler = new Handler();
        final UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        final AppChecker appChecker = new AppChecker();

        final String lastAppPkgName = "";

        handler.post(new Runnable() {
            @Override
            public void run(){

                refreshUsageStats();

                String currentApp = appChecker.getForegroundApp(context);


                if(apps.get(currentApp).isTracked()){
//                    show();
                } else {
//                    hide();
                }

                handler.postDelayed(this, 10000);
            }
        });
    }

    public class tracking implements Runnable {
        private String lastPkgName;

        //https://stackoverflow.com/questions/3873659/android-how-can-i-get-the-current-foreground-activity-from-a-service/27642535
        final Handler handler = new Handler();

        @Override
        public void run() {
//            refreshUsageStats();

            try {
                lastPkgName = updateOverlay(lastPkgName);
            } catch (NullPointerException e){
                lastPkgName = "none";
            }

            if(!lastSavedDate.equals(getDateString())){
                Log.d("SAVING", "SAVING");
                saveAppsAverageUsageLastTwoWeeks();
            } else {
                Log.d("NOTTTT", "SAVVVVIIIINNNNGGG");
            }

            handler.postDelayed(this, interval);
        }
    }

    private String updateOverlay(String lastPkgName){
        //            getScreenState?

        currentApp = appChecker.getForegroundApp(TrackingService.this);

//            Log.d("Tracked usage", quotaPercentageUsed() + "");
//            Log.d("recent percentage ", recentPercentage() + "");


        if(lastPkgName == null){
            lastPkgName = "none";
        }

        Log.d("Hmmmm", "lastPkgName = " + lastPkgName + ", currentApp = " + currentApp);

//            if(currentApp == null){
//                currentApp
//            }

        if(currentApp.equals(lastPkgName)){
            if(isTracked(currentApp)){
                updateTrackedGlow();
            } else {
                updateNonTrackedGlow();
            }
        } else {
            if(isTracked(currentApp)){
                updateTrackedGlow();
            }
        }

        mBuilder.setContentText("You have used " + (int) (quotaPercentageUsed() * 100) + "% of your daily allowance.");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        return currentApp;
    }


    private void updateTrackedGlow(){
        float recentPercentage = recentPercentage();
        int glowHeight = (int) (((double) height) * quotaPercentageUsed());
        overlay.findViewById(R.id.innerGlow).getLayoutParams().height = glowHeight;
        show(overlay.findViewById(R.id.outerGlow), recentPercentage);
        overlay.findViewById(R.id.outerGlow).getLayoutParams().height = glowHeight;
        overlay.findViewById(R.id.fadingEdge).getLayoutParams().height = glowHeight;


        int newColor = getColorFromPercentage(recentPercentage);

//        ((BottomCropImage) overlay.findViewById(R.id.innerGlow)).setColorFilter(newColor);
        ValueAnimator anim = ValueAnimator.ofArgb(colourFilterColour, newColor);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ((ImageView) overlay.findViewById(R.id.innerGlow)).setColorFilter((int) animation.getAnimatedValue());
                ((ImageView) overlay.findViewById(R.id.outerGlow)).setColorFilter((int) animation.getAnimatedValue());
            }
        });
        anim.setDuration(interval);
        anim.start();
        overlay.getRootView().requestLayout();
        Log.d("Color", "New color");

        colourFilterColour = newColor;

    }

    private void updateNonTrackedGlow(){
        int glowHeight = (int) (((double) height) * quotaPercentageUsed());
        hide(overlay.findViewById(R.id.outerGlow));
        overlay.findViewById(R.id.innerGlow).getLayoutParams().height = glowHeight;
        overlay.findViewById(R.id.outerGlow).getLayoutParams().height = glowHeight;
        overlay.findViewById(R.id.fadingEdge).getLayoutParams().height = glowHeight;
        Log.d("New Height", ""+overlay.findViewById(R.id.innerGlow).getLayoutParams().height);
        ValueAnimator anim = ValueAnimator.ofArgb(colourFilterColour, Color.parseColor("#00000000"));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ((ImageView) overlay.findViewById(R.id.innerGlow)).setColorFilter((int) animation.getAnimatedValue());
                ((ImageView) overlay.findViewById(R.id.innerGlow)).setColorFilter((int) animation.getAnimatedValue());
            }
        });
        anim.setDuration(interval);
        anim.start();
        overlay.getRootView().requestLayout();

        colourFilterColour = Color.parseColor("#00000000");
    }

    //https://developer.android.com/training/animation/reveal-or-hide-view
    private void hide(final View v){
//        v.setAlpha(1f);
        v.animate()
                .alpha(0f)
                .setDuration(interval)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                    }
                });
        visible = false;
    }

    //https://developer.android.com/training/animation/reveal-or-hide-view
    private void show(View v, float newAlpha){
//        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);
        v.animate()
                .alpha(newAlpha)
                .setDuration(interval)
                .setListener(null);

        visible = true;
    }

    private boolean isTracked(String app){
        if(apps.containsKey(app)){
            if(Objects.requireNonNull(apps.get(app)).isTracked()){
                return true;
            }
        }
        return false;
    }


    //main suggestion https://stackoverflow.com/a/61595525/3032936
    //some influence https://stackoverflow.com/a/50647945/3032936
    public HashMap<String, AppUsageInfo> queryUsageStatistics(Context context, long startTime, long endTime) {

        long currentTime = System.currentTimeMillis();

        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsageInfo> map = new HashMap<>();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        assert mUsageStatsManager != null;
        // Here we query the events from startTime till endTime.
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        // go over all events.
        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            String packageName = currentEvent.getPackageName();
            if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                    || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                    || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED
                    || currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND
                    || currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent); // an extra event is found, add to all events list.
                // taking it into a collection to access by package name
                if (!map.containsKey(packageName)) {
                    map.put(packageName, new AppUsageInfo());
                }
            }
        }

        Collections.sort(allEvents, new EventComparator());

        HashMap<String, Long> appEvents = new HashMap<>();

        for (int i = 0; i < allEvents.size(); i++) {
            UsageEvents.Event event = allEvents.get(i);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                || event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND){
                if(!appEvents.containsKey(event.getPackageName())){
                    appEvents.put(event.getPackageName(), event.getTimeStamp());
                }
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                        || event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND){
                if(appEvents.containsKey(event.getPackageName())){
                    long diff = event.getTimeStamp() - appEvents.get(event.getPackageName());
//                    Log.d("EVENT: " , " " + diff);
                    Objects.requireNonNull(map.get(event.getPackageName())).timeInForeground += diff;
//                    if(event.getPackageName().equals("com.android.chrome")){
//                        Log.d("SPOTIFY", String.valueOf(diff));
//                    }
                    appEvents.remove(event.getPackageName());
                } else {
                    if(map.get(event.getPackageName()).getTimeInForeground() == 0){
                        long diff = event.getTimeStamp() - startTime;
//                        if(event.getPackageName().equals("com.android.chrome")){
//                            Log.d("SPOTIFY ..", String.valueOf(diff));
//                        }
                        Objects.requireNonNull(map.get(event.getPackageName())).timeInForeground += diff;
                    }
                }
            }
        }

        //checking if any have a start time but never found an end time -- these apps are still running and we need to add the length of the ongoing session
        for(String packageName : appEvents.keySet()){
            long diff = currentTime - appEvents.get(packageName);
            Objects.requireNonNull(map.get(packageName)).timeInForeground += diff;
        }

        return map;
    }

    public HashMap<String, TrackedApp> getTrackedAppsData(){
        return apps;
    }

    public ArrayList<String> getTrackedAppsFromPrefs(){
        String masterString = sharedPref.getString("tracked-apps", "");
        Log.d("Saved preferences:", masterString);
        return new ArrayList<>(Arrays.asList(masterString.split("@")));
    }

    public void saveTrackedApps(){
        Log.d("Saving preferences:", "here");
        String trackedAppString = "";
        for(TrackedApp tApp : apps.values()){
            if(tApp.isTracked()){
                trackedAppString = trackedAppString.concat(tApp.getPackageName() + "@");
            }
        }
        Log.d("Saving preferences:", trackedAppString);
        sharedPref.edit().putString("tracked-apps", trackedAppString).apply();

    }

    private float recentPercentage(){
        Log.d("Calculation: ", (float) trackedUsageInLast(heavyUseInterval * 60) + " / " + (float) (heavyUseInterval * 60));
        return (float) trackedUsageInLast(heavyUseInterval * 60) / (float) (heavyUseInterval * 60);
    }

    private long trackedUsageInLast(long seconds){
        long millis = seconds*1000;
        long usage = 0;

        Map<String, AppUsageInfo> usageStatsMap = queryUsageStatistics(this, System.currentTimeMillis() - millis, System.currentTimeMillis());

        for (TrackedApp tApp : apps.values()){
            if(tApp.isTracked()){
                if(usageStatsMap.get(tApp.getPackageName()) != null){
                    usage += usageStatsMap.get(tApp.getPackageName()).getTimeInForeground();
                }
            }
        }

        if(apps.containsKey(currentApp)){
            if(apps.get(currentApp).isTracked()){
                if(usage == 0){
                    usage = seconds * 1000;
                }
            }
        }

        return usage / 1000;
    }

    private long trackedUsageThisDaySeconds(){
        Map<String, AppUsageInfo> usageStatsMap = getUsageInfoThisDay();
        long usage = 0;

        for (TrackedApp tApp : apps.values()){
            if(tApp.isTracked()){
                if(usageStatsMap.get(tApp.getPackageName()) != null){
                    usage += usageStatsMap.get(tApp.getPackageName()).getTimeInForeground();
                }
            }
        }

        return usage / 1000;
    }

    private void setAverageUsageLastWeek(){

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - 604800000, System.currentTimeMillis());

        HashMap<String, ArrayList<Long>> packageUsages = new HashMap<>();

        for(UsageStats u : usageStatsList){
            if(!packageUsages.containsKey(u.getPackageName())) {
                packageUsages.put(u.getPackageName(), new ArrayList());
            }
            packageUsages.get(u.getPackageName()).add(u.getTotalTimeInForeground());
        }

        for(Map.Entry<String, ArrayList<Long>> p : packageUsages.entrySet()){
            if(apps.containsKey(p.getKey())){
                int i = 0;
                double tot = 0;
                for(long t : p.getValue()){
                    tot += t;
                    i++;
                }
                apps.get(p.getKey()).setAvgUsageLastWeek(tot / i);
            }
        }
    }

    public long trackedAppsAverageUsageLastWeek(){
        long tot = 0;
        for (TrackedApp tApp : apps.values()){
            if(tApp.isTracked()){
                tot += tApp.getAvgUsageLastWeek();
            }
        }
        return tot;
    }

    public void saveAppsAverageUsageLastTwoWeeks(){

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - (604800000 * 2), System.currentTimeMillis());

        HashMap<String, ArrayList<Long>> packageUsages = new HashMap<>();

        String text = "";

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        for(UsageStats u : usageStatsList){
            String dateString = formatter.format(new Date(u.getFirstTimeStamp()));

            String name;
            String tracked;

            if(apps.containsKey(u.getPackageName())){
                name = apps.get(u.getPackageName()).getName();
                tracked = Boolean.toString(apps.get(u.getPackageName()).isTracked());
            } else {
                name = u.getPackageName();
                tracked = "false";
            }

            text = text.concat(dateString + "," + u.getPackageName() + "," + name + "," + u.getTotalTimeInForeground() + "," + tracked + "\n");
        }


        Date todayDate = Calendar.getInstance().getTime();
        String todayString = formatter.format(todayDate);

        String fileName = todayString + "_usage.csv";

        FileOutputStream fos = null;

        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e){
                    e.printStackTrace();;
                }
            }
        }

        sharedPref.edit().putString("dateLastSaved", todayString).apply();
        lastSavedDate = todayString;

        uploadUsage(fileName);
    }

    private double quotaPercentageUsed(){
        long trackedUsageThisDaySeconds = trackedUsageThisDaySeconds();
        return Math.min((double) trackedUsageThisDaySeconds / (dailyQuotaMinutes * 60), 1);
    }

    private int getColorFromPercentage(float percentage){
        float max = 0f;
        float min = 60f;

        float rangeSize = Math.abs(max-min);

        float hue = min - (rangeSize * percentage);

        return ColorUtils.setAlphaComponent(Color.HSVToColor(new float[]{hue, 1.0f, 1.0f}), (int) (255 * percentage));
    }

    public void setQuota(long mills){
        sharedPref.edit().putLong("quota", mills).apply();
        loadSavedQuota();
    }

    public int getQuota(){
        return dailyQuotaMinutes;
    }

    private void loadSavedQuota(){
        dailyQuotaMinutes = (int) sharedPref.getLong("quota", 0) / 1000 / 60;
        Log.d("debug", dailyQuotaMinutes + " ");
    }

    private void uploadUsage(String filename){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d("TRYING", id + " Trying to upload.");

        Uri file = Uri.fromFile(new File(getFilesDir() + "/" + filename));
        StorageReference riversRef = storageRef.child("usage/" + id + "/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        Log.d("FAILURE?", "FAILURE?");

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FAILURE", "FAILURE");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("SUCCESS", "SUCCESS");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }

    private String getDateString(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date todayDate = Calendar.getInstance().getTime();
        return formatter.format(todayDate);
    }

}





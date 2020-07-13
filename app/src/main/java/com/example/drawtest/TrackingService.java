package com.example.drawtest;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.room.Room;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//https://stackoverflow.com/questions/55045005/draw-overlay-underneath-navigation-bar
//https://gist.github.com/MaTriXy/9f291bccd8123a5ae8e6cb9e21f627ff
//https://proandroiddev.com/bound-and-foreground-services-in-android-a-step-by-step-guide-5f8362f4ae20

public class TrackingService extends Service implements OnTouchListener, OnClickListener {

    private View topLeftView;

    private View overlay;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private WindowManager wm;

    private int startHour = 23;
    private int startMinute = 30;

    HashMap<String, TrackedApp> trackedApps;
    private TrackedAppDatabase db;
    private SharedPreferences sharedPref;
    ArrayList<String> trackedAppCodes;

    private boolean visible;

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
        sharedPref = getSharedPreferences("tracked-apps", Context.MODE_PRIVATE);
        trackedAppCodes = getTrackedAppsFromPrefs();

        int LAYOUT_FLAG;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        overlay = new View(this);
        overlay.setOnTouchListener(this);
        overlay.setBackground(getDrawable(R.drawable.border));
        overlay.setOnClickListener(this);

        WindowManager.LayoutParams params = new LayoutParams(1080,
                1920,
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

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        wm.addView(topLeftView, topLeftParams);

        Toast.makeText(this, "Hey!", Toast.LENGTH_SHORT).show();


        String NOTIFICATION_CHANNEL_ID = getPackageName();
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Overlay Channel", NotificationManager.IMPORTANCE_LOW);

        NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, TrackingService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        android.app.Notification notification =  new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("A Notification")
                .setContentText("Notice me!")
                .setSmallIcon(R.drawable.psychology_24px)
                .setContentIntent(pendingIntent)
                .setTicker("Oi oi!")
                .setColorized(true)
                .setColor(getResources().getColor(R.color.colorAccent))
                .build();
        startForeground(1, notification);

        visible = true;

        trackedApps = new HashMap<>();

        importAppsList();

        refreshUsageStats();

        runTracking(this);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Log.d(this.getPackageName(), "hey");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay != null) {
            wm.removeView(overlay);
            wm.removeView(topLeftView);
            overlay = null;
            topLeftView = null;
        }
        Log.d("HEY", "----------------------");
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            overlay.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
            System.out.println("originalY="+originalYPos);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (LayoutParams) overlay.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            wm.updateViewLayout(overlay, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving) {
                return true;
            }
        }

//        overlayedButton.setWidth(overlayedButton.getWidth() + 1);

        return false;
    }

    @Override
    public void onClick(View v) {
//        Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();
    }

    public void makeBlue() {
        ShapeDrawable shapeDrawable = (ShapeDrawable) overlay.getBackground();

        shapeDrawable.getPaint().setColor(Color.BLUE);
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

    public void hide(){
        overlay.setAlpha(0);
        visible = false;
    }

    public void show(){
        overlay.setAlpha(1);
        visible = true;
    }


    public void importAppsList(){
        PackageManager pm = getPackageManager();
        for(ApplicationInfo a : pm.getInstalledApplications(0)){
            if(pm.getLaunchIntentForPackage(a.packageName) != null) {
                trackedApps.put(a.packageName, new TrackedApp(a.loadLabel(pm).toString(), a.packageName, a.loadIcon(pm), 0));
                if(trackedAppCodes.contains(a.packageName)){
                    trackedApps.get(a.packageName).setTracked(true);
                }
            }
        }
    }

    public void refreshUsageStats(){
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendar startCal;
        Log.d("option", cal.get(Calendar.HOUR_OF_DAY) + " ");

        if((cal.get(Calendar.HOUR_OF_DAY) < startHour) || ((cal.get(Calendar.HOUR_OF_DAY) == startHour) && (cal.get(Calendar.MINUTE) < startMinute))){
            Log.d("option", "A");
            cal.add(Calendar.DATE, -1);
            startCal = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    startHour,
                    startMinute,
                    0);
            Log.d("date", startCal.getTime().toString());
        } else {
            Log.d("option", "B");

            startCal = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH),
                    startHour,
                    startMinute,
                    0);
            Log.d("date", startCal.getTime().toString());
        }

        long startTime = System.currentTimeMillis();
        Map<String, AppUsageInfo> usageStatsMap = queryUsageStatistics(this, startCal.getTimeInMillis(), System.currentTimeMillis());
        Log.d("Time: ", "" + ((double) System.currentTimeMillis() - (double) startTime));


//        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(startCal.getTimeInMillis(), System.currentTimeMillis());

        for(TrackedApp a : trackedApps.values()){
            if(usageStatsMap.get(a.getPackageName()) != null){
                a.setUsageToday(usageStatsMap.get(a.getPackageName()).getTimeInForeground());
            }
        }
    }

    public void runTracking(final Context context){
        //https://stackoverflow.com/questions/3873659/android-how-can-i-get-the-current-foreground-activity-from-a-service/27642535
        final Handler handler = new Handler();
        final UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        handler.post(new Runnable() {
            @Override
            public void run(){

                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();

                AppChecker appChecker = new AppChecker();

//                if(trackedApps.get(appChecker.getForegroundApp(context)))

                if(appChecker.getForegroundApp(context).equals("com.facebook.orca")){
                    hide();
                } else {
                    show();
                }

//                Log.d("Currently running", "" + appChecker.getForegroundApp(context));

                refreshUsageStats();

                String currentApp = appChecker.getForegroundApp(context);

                if(trackedApps.get(currentApp).isTracked()){
                    Toast.makeText(context, "Tracking!", Toast.LENGTH_SHORT).show();
                }

//                if(trackedApps.containsKey(currentApp)) {
//
//                    long usage = trackedApps.get(currentApp).getUsageToday();
//                    Toast.makeText(context, (usage / 1000) + " seconds", Toast.LENGTH_SHORT).show();
//                }

//                Log.d("Hey", "" + appChecker.getForegroundApp(context));
                handler.postDelayed(this, 10000);
            }
        });
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
            if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED ||
                    currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED) {
//                if(packageName.equals("com.DanVogt.DATAWING")){
//                    Log.d("DataWing:", currentEvent.getEventType() + " " + currentEvent.getTimeStamp()/1000);
//                }
                allEvents.add(currentEvent); // an extra event is found, add to all events list.
                // taking it into a collection to access by package name
                if (!map.containsKey(packageName)) {
                    map.put(packageName, new AppUsageInfo());
                }
            }
        }

        Collections.sort(allEvents, new EventComparator());

        HashMap<String, Long> appEvents = new HashMap<>();

        for (int i = 0; i < allEvents.size() - 1; i++) {
            UsageEvents.Event event = allEvents.get(i);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                if(!appEvents.containsKey(event.getPackageName())){
                    appEvents.put(event.getPackageName(), event.getTimeStamp());
                }
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED){
                if(appEvents.containsKey(event.getPackageName())){
                    long diff = event.getTimeStamp() - appEvents.get(event.getPackageName());
//                    Log.d("EVENT: " , " " + diff);
                    Objects.requireNonNull(map.get(event.getPackageName())).timeInForeground += diff;
                    appEvents.remove(event.getPackageName());
                }
            }
        }

        //checking if any have a start time but never found an end time -- these apps are still running and we need to add the length of the ongoing session
        for(String packageName : appEvents.keySet()){
            long diff = currentTime - appEvents.get(packageName);
            Objects.requireNonNull(map.get(packageName)).timeInForeground += diff;
        }

        // iterate through all events.
//        for (int i = 0; i < allEvents.size() - 1; i++) {
//            UsageEvents.Event event0 = allEvents.get(i);
//            UsageEvents.Event event1 = allEvents.get(i + 1);
//
//            Log.d("Events:", event0.getPackageName() + " " + event0.getEventType() + " " + event0.getTimeStamp() / 1000);
//            //for launchCount of apps in time range
//            if (!event0.getPackageName().equals(event1.getPackageName()) && event1.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
//                // if true, E1 (launch event of an app) app launched
//                Objects.requireNonNull(map.get(event1.getPackageName())).launchCount++;
//            }
//
//            //for UsageTime of apps in time range
//            if (event0.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED &&
//                    (event1.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED || event1.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED)
//                    && event0.getPackageName().equals(event1.getPackageName())) {
//                long diff = event1.getTimeStamp() - event0.getTimeStamp();
//                Objects.requireNonNull(map.get(event0.getPackageName())).timeInForeground += diff;
//            }
//        }
        // and return the map.
        return map;
    }

    public HashMap<String, TrackedApp> getTrackedAppsData(){
        return trackedApps;
    }

    public ArrayList<String> getTrackedAppsFromPrefs(){
        String masterString = sharedPref.getString("tracked-apps", "");
        Log.d("Saved preferences:", masterString);
        return new ArrayList<>(Arrays.asList(masterString.split("@")));
    }

    public void saveTrackedApps(){
        Log.d("Saving preferences:", "here");
        String trackedAppString = "";
        for(TrackedApp tApp : trackedApps.values()){
            if(tApp.isTracked()){
                trackedAppString = trackedAppString.concat(tApp.getPackageName() + "@");
            }
        }
        Log.d("Saving preferences:", trackedAppString);
        sharedPref.edit().putString("tracked-apps", trackedAppString).apply();

    }

}

package com.jminton.apptracker;

import android.graphics.drawable.Drawable;

public class TrackedApp {

    private String packageName;
    private String name;
    private Drawable icon;
    private long googleUsageToday;
    private long usageToday;
    private boolean isTracked;
    private double avgUsageLastWeek;

    public TrackedApp(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.usageToday = 0;
        this.googleUsageToday = 0;
    }

    public TrackedApp(String name, String packageName, Drawable icon, long usageToday, long googleUsageToday) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.usageToday = usageToday;
        this.googleUsageToday = googleUsageToday;
        this.isTracked = false;

    }

    public TrackedApp(String name, String packageName, Drawable icon, long usageToday) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.usageToday = usageToday;
        this.googleUsageToday = 0;
        this.isTracked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getUsageToday() {
        return usageToday;
    }

    public void setUsageToday(long usageToday) {
        this.usageToday = usageToday;
    }

    public long getGoogleUsageToday() {
        return googleUsageToday;
    }

    public void setGoogleUsageToday(long googleUsageToday) {
        this.googleUsageToday = googleUsageToday;
    }

    public boolean isTracked() {
        return isTracked;
    }

    public void setTracked(boolean tracked) {
        isTracked = tracked;
    }

    public double getAvgUsageLastWeek() {
        return avgUsageLastWeek;
    }

    public void setAvgUsageLastWeek(double avgUsageLastWeek) {
        this.avgUsageLastWeek = avgUsageLastWeek;
    }
}

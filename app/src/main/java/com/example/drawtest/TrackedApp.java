package com.example.drawtest;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class TrackedApp {
    @PrimaryKey
    @NonNull private String packageName;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "icon") private Drawable icon;
    @ColumnInfo(name = "googleUsageToday") private long googleUsageToday;
    @ColumnInfo(name = "usageToday") private long usageToday;
    @ColumnInfo(name = "isTracked") private boolean isTracked;

    public TrackedApp(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }

    public TrackedApp(String name, String packageName, Drawable icon, long usageToday, long googleUsageToday) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.usageToday = usageToday;
        this.googleUsageToday = googleUsageToday;
    }

    public TrackedApp(String name, String packageName, Drawable icon, long usageToday) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.usageToday = usageToday;
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
}

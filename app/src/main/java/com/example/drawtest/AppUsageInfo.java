package com.example.drawtest;

public class AppUsageInfo {

    public long timeInForeground;
    public int launchCount;

    AppUsageInfo() {
        this.timeInForeground = 0;
        this.launchCount = 0;
    }

    public long getTimeInForeground() {
        return timeInForeground;
    }

    public void setTimeInForeground(long timeInForeground) {
        this.timeInForeground = timeInForeground;
    }

    public int getLaunchCount() {
        return launchCount;
    }

    public void setLaunchCount(int launchCount) {
        this.launchCount = launchCount;
    }
}
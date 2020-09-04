package com.jminton.apptracker;

//taken from https://stackoverflow.com/a/61595525/3032936 in combination with queryUsageStatistics() method in TrackingService class.
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
package com.jminton.apptracker;

import android.app.usage.UsageEvents;
import java.util.Comparator;

public class EventComparator implements Comparator<UsageEvents.Event> {
    @Override
    public int compare(UsageEvents.Event o1, UsageEvents.Event o2) {
        if (o1.getTimeStamp() > o2.getTimeStamp()) {
            return 1;
        } else if (o1.getTimeStamp() < o2.getTimeStamp()) {
            return -1;
        } else {
            return 0;
        }
    }
}

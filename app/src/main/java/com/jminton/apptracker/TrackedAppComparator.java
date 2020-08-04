package com.jminton.apptracker;


import java.util.Comparator;

public class TrackedAppComparator implements Comparator<TrackedApp> {

    @Override
    public int compare(TrackedApp o1, TrackedApp o2) {

        if(o1.getAvgUsageLastWeek() > o2.getAvgUsageLastWeek()){
            return -1;
        } else if(o2.getAvgUsageLastWeek() > o1.getAvgUsageLastWeek()){
            return 1;
        } else {
            return 0;
        }
    }
}
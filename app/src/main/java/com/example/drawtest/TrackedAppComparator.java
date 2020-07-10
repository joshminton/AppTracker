package com.example.drawtest;


import java.util.Comparator;

public class TrackedAppComparator implements Comparator<TrackedApp> {

    @Override
    public int compare(TrackedApp o1, TrackedApp o2) {

        if(o1.getUsageToday() > o2.getUsageToday()){
            return -1;
        } else if(o2.getUsageToday() > o1.getUsageToday()){
            return 1;
        } else {
            return 0;
        }
    }
}
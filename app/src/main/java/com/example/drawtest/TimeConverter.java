package com.example.drawtest;

public class TimeConverter {
    public static String millsToHoursMinutesSeconds(long mills){
        double hours = Math.floor(mills/1000/60/60);
        mills -= hours * 60 * 60 * 1000;
        double minutes = Math.floor(mills/1000/60);
        mills -= minutes * 60 * 1000;
        double seconds = Math.floor(mills/1000);
        mills -= seconds * 1000;

        return ((hours < 10) ? "0" : "") + (int) hours +":"+ ((minutes < 10) ? "0" : "") + (int) minutes+":"+ ((seconds < 10) ? "0" : "") + (int) seconds;

    }

    public static String millsToHoursMinutesSecondsVerbose(long mills){
        double hours = Math.floor(mills/1000/60/60);
        mills -= hours * 60 * 60 * 1000;
        double minutes = Math.floor(mills/1000/60);
        mills -= minutes * 60 * 1000;
        double seconds = Math.floor(mills/1000);
        mills -= seconds * 1000;

        return ((hours != 0) ? ((hours < 10) ? "0" : "" + (int) hours) +" hours " : "") + ((minutes < 10) ? "0" : "") + (int) minutes+" minutes";

    }


}

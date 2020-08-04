package com.jminton.apptracker;

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

        if(mills < 60000){
            return "less than a minute";
        }

        double hours = Math.floor(mills/1000/60/60);
        mills -= hours * 60 * 60 * 1000;
        double minutes = Math.floor(mills/1000/60);
        mills -= minutes * 60 * 1000;
        double seconds = Math.floor(mills/1000);
        mills -= seconds * 1000;

        String hourString;
        String minuteString;

        if(hours > 1) {
            hourString = " hours ";
        } else {
            hourString = " hour ";
        }

        if(minutes > 1) {
            minuteString = " minutes ";
        } else {
            minuteString = " minute ";
        }

        return ((hours != 0) ? (int) hours +" hours " : "") + ((minutes < 10) ? "0" : "") + (int) minutes+" minutes";

    }
}

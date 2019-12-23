package com.example.mymusic.Utils;

public class Utils {
    public static String intToTime(int time){
        int seconds = time/1000;
        int minutes = seconds/60;
        int hours = minutes /60;
        minutes %=60;
        seconds %= 60;
        if (hours!=0) return String.format("%02d:%02d:%02d",hours,minutes,seconds);
        else return String.format("%02d:%02d",minutes,seconds);
    }
}

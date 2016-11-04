package com.yagi2.eorzeawatchface;

import java.util.concurrent.TimeUnit;

public class ConvertLTtoET {
    final private static double EORZEA_TIME_DIFF = 20.571428571;

    public static class EorzeaTime {
        public int hour;
        public int min;
        public long sec;

        public EorzeaTime() {
            this.hour = 0;
            this.min = 0;
            this.sec = 0;
        }
    }

    public static EorzeaTime getEorzeaNowTime(long now) {
        EorzeaTime displayTime = new EorzeaTime();
        EorzeaTime calcTime = getSpendEorzeaTime(now);

        displayTime.hour = calcTime.hour % 24;
        displayTime.min  = calcTime.min  % 60;
        displayTime.sec  = calcTime.sec  % 60;

        return displayTime;
    }

    private static EorzeaTime getSpendEorzeaTime(long now) {
        EorzeaTime result = new EorzeaTime();

        double eorzeaSpendSec = TimeUnit.MILLISECONDS.toSeconds(now) * EORZEA_TIME_DIFF;

        result.sec   = (long)eorzeaSpendSec;
        result.min   = (int)(result.sec / 60);
        result.hour  = result.min / 60;

        return result;
    }

}

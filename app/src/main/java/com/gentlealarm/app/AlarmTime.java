package com.gentlealarm.app;

import java.util.Calendar;
import java.util.TimeZone;

final class AlarmTime {
    private AlarmTime() { }

    static long nextTime(long now, int hour, int minute, TimeZone zone) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid local alarm time");
        }
        Calendar date = Calendar.getInstance(zone);
        date.setTimeInMillis(now);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        if (date.getTimeInMillis() <= now + 1000) date.add(Calendar.DAY_OF_YEAR, 1);
        return date.getTimeInMillis();
    }
}

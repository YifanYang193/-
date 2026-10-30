package com.gentlealarm.app;

import java.util.Calendar;
import java.util.TimeZone;

/** Invariants that matter for a wake-up alarm, executable without an Android SDK. */
public final class CoreChecks {
    private static void require(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }

    public static void main(String[] args) {
        for (float ceiling : new float[]{.1f, .6f, 1f}) {
            float previous = 0;
            for (int step = 0; step <= 2000; step++) {
                float gain = FadeEnvelope.gain(step / 1000.0, ceiling);
                require(gain >= previous && gain >= .01f && gain <= ceiling + .000001f, "gain bounds/monotonicity");
                previous = gain;
            }
            require(Math.abs(FadeEnvelope.gain(0, ceiling) - .01f) < .000001f, "quiet start");
            require(Math.abs(FadeEnvelope.gain(1, ceiling) - ceiling) < .000001f, "ceiling reached");
            require(FadeEnvelope.gain(4, ceiling) == FadeEnvelope.gain(1, ceiling), "hold ceiling after ramp");
            require(FadeEnvelope.gain(4.01 / 60, ceiling) >= FadeEnvelope.gain(3.99 / 60, ceiling), "loop boundary continuity");
        }
        TimeZone zone = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar now = Calendar.getInstance(zone);
        now.clear(); now.set(2026, Calendar.OCTOBER, 29, 23, 59, 30);
        Calendar target = Calendar.getInstance(zone);
        target.setTimeInMillis(AlarmTime.nextTime(now.getTimeInMillis(), 7, 0, zone));
        require(target.get(Calendar.DAY_OF_MONTH) == 30 && target.get(Calendar.HOUR_OF_DAY) == 7, "midnight rollover");
        now.clear(); now.set(2026, Calendar.DECEMBER, 31, 23, 59, 30);
        target.setTimeInMillis(AlarmTime.nextTime(now.getTimeInMillis(), 0, 0, zone));
        require(target.get(Calendar.YEAR) == 2027 && target.get(Calendar.DAY_OF_MONTH) == 1, "year rollover");
        now.clear(); now.set(2026, Calendar.OCTOBER, 29, 6, 0, 0);
        target.setTimeInMillis(AlarmTime.nextTime(now.getTimeInMillis(), 7, 0, zone));
        require(target.get(Calendar.DAY_OF_MONTH) == 29, "same day when future");
        System.out.println("Core checks passed: gain bounds, monotonicity, loop continuity, midnight/year rollover, same-day scheduling.");
    }
}

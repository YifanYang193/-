package com.gentlealarm.app;

import android.content.Context;
import android.content.Intent;

final class AlarmActions {
    static final String STOP = "com.gentlealarm.app.STOP";
    static final String SNOOZE = "com.gentlealarm.app.SNOOZE";

    static void stop(Context context) {
        new AlarmState(context).setRinging(false);
        context.stopService(new Intent(context, AlarmService.class));
    }

    static void snooze(Context context) {
        // Schedule first. If permission was revoked, keep the current alarm ringing.
        AlarmScheduler.schedule(context, System.currentTimeMillis() + 5 * 60_000L, true);
        stop(context);
        new AlarmState(context).note("已贪睡 5 分钟");
    }
}

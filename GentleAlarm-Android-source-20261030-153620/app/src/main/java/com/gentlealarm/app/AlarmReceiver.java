package com.gentlealarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public final class AlarmReceiver extends BroadcastReceiver {
    private static PowerManager.WakeLock handoffLock;

    @Override public void onReceive(Context context, Intent intent) {
        AlarmState state = new AlarmState(context);
        long expected = intent.getLongExtra(AlarmScheduler.EXTRA_TIME, -1);
        if (!state.enabled() || expected != state.scheduledAt()) return;
        state.disable("闹钟已触发");
        handoffLock = context.getSystemService(PowerManager.class)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GentleAlarm:handoff");
        handoffLock.acquire(15000);
        try {
            context.startForegroundService(new Intent(context, AlarmService.class));
        } catch (RuntimeException error) {
            releaseHandoff();
            state.note("系统未允许启动响铃：" + error.getMessage());
        }
    }

    static void releaseHandoff() {
        if (handoffLock != null && handoffLock.isHeld()) handoffLock.release();
        handoffLock = null;
    }
}

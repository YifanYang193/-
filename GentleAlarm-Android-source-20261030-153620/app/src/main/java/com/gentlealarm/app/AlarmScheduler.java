package com.gentlealarm.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.TimeZone;

final class AlarmScheduler {
    static final String EXTRA_TIME = "scheduled_time";

    static boolean canSchedule(Context context) {
        AlarmManager manager = context.getSystemService(AlarmManager.class);
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms();
    }

    static long nextTime(int hour, int minute) {
        return AlarmTime.nextTime(System.currentTimeMillis(), hour, minute, TimeZone.getDefault());
    }

    static void schedule(Context context, long time, boolean relative) {
        if (!canSchedule(context)) throw new IllegalStateException("请先允许“闹钟和提醒”权限");
        if (time <= System.currentTimeMillis()) throw new IllegalArgumentException("响铃时间已经过去");
        PendingIntent show = PendingIntent.getActivity(context, 100,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        context.getSystemService(AlarmManager.class).setAlarmClock(
                new AlarmManager.AlarmClockInfo(time, show), operation(context, time));
        new AlarmState(context).scheduled(time, relative);
    }

    private static PendingIntent operation(Context context, long time) {
        Intent intent = new Intent(context, AlarmReceiver.class).putExtra(EXTRA_TIME, time);
        return PendingIntent.getBroadcast(context, 101, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void cancel(Context context) {
        context.getSystemService(AlarmManager.class).cancel(operation(context, 0));
        new AlarmState(context).disable("已取消闹钟");
    }

    static void restore(Context context, boolean adjustLocalTime) {
        AlarmState state = new AlarmState(context);
        if (!state.enabled()) return;
        if (!canSchedule(context)) {
            state.note("定时权限未开启，闹钟尚未恢复；请打开应用处理");
            return;
        }
        long time = adjustLocalTime && !state.relativeTime()
                ? nextTime(state.hour(), state.minute()) : state.scheduledAt();
        if (time <= System.currentTimeMillis()) {
            state.disable("关机或系统时间变化期间错过了闹钟，请重新设置");
            return;
        }
        schedule(context, time, state.relativeTime());
    }
}

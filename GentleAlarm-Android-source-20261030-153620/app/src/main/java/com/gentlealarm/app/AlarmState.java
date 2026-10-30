package com.gentlealarm.app;

import android.content.Context;
import android.content.SharedPreferences;

/** Only non-sensitive alarm settings are stored in device-protected storage. */
final class AlarmState {
    private final SharedPreferences preferences;

    AlarmState(Context context) {
        preferences = context.createDeviceProtectedStorageContext()
                .getSharedPreferences("gentle_alarm_v2", Context.MODE_PRIVATE);
    }

    int hour() { return preferences.getInt("hour", 7); }
    int minute() { return preferences.getInt("minute", 0); }
    int fadeSeconds() { return preferences.getInt("fade", 60); }
    float ceiling() { return preferences.getFloat("ceiling", .6f); }
    long scheduledAt() { return preferences.getLong("scheduled", 0); }
    boolean enabled() { return preferences.getBoolean("enabled", false); }
    boolean relativeTime() { return preferences.getBoolean("relative", false); }
    boolean ringing() { return preferences.getBoolean("ringing", false); }
    String note() { return preferences.getString("note", "尚未设置闹钟"); }

    void saveOptions(int hour, int minute, int fade, float ceiling) {
        preferences.edit().putInt("hour", hour).putInt("minute", minute)
                .putInt("fade", fade).putFloat("ceiling", ceiling).commit();
    }

    void scheduled(long time, boolean relative) {
        preferences.edit().putLong("scheduled", time).putBoolean("enabled", true)
                .putBoolean("relative", relative)
                .putString("note", "已交给系统等待唤醒").commit();
    }

    void disable(String note) {
        preferences.edit().putBoolean("enabled", false).putString("note", note).commit();
    }

    void setRinging(boolean ringing) {
        preferences.edit().putBoolean("ringing", ringing).commit();
    }

    void note(String note) {
        preferences.edit().putString("note", note).commit();
    }
}

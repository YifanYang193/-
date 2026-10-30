package com.gentlealarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

public final class AlarmService extends Service {
    static boolean running;
    static final String CHANNEL = "gentle_alarm_playback_v1";
    private static final int NOTIFICATION_ID = 20;
    static final long MAX_RING_MILLIS = 10 * 60_000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final FadingPlayer player = new FadingPlayer();
    private PowerManager.WakeLock wakeLock;
    private boolean started;

    static void createChannel(android.content.Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL, "闹钟响铃", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("响铃时显示停止和贪睡。声音由渐强播放器负责。");
        channel.setSound(null, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
        channel.enableVibration(false);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (started) return START_NOT_STICKY;
        started = true;
        AlarmState state = new AlarmState(this);
        try {
            createChannel(this);
            Intent ringIntent = new Intent(this, RingingActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent screen = PendingIntent.getActivity(this, 200, ringIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new Notification.Builder(this, CHANNEL)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle("早安，慢慢醒来")
                    .setContentText("声音正在逐渐增强，点击可停止或贪睡")
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setContentIntent(screen)
                    .setFullScreenIntent(screen, true)
                    .addAction(new Notification.Action.Builder(null, "停止", action(AlarmActions.STOP, 201)).build())
                    .addAction(new Notification.Action.Builder(null, "贪睡 5 分钟", action(AlarmActions.SNOOZE, 202)).build())
                    .build();
            if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else startForeground(NOTIFICATION_ID, notification);
            running = true;
            wakeLock = getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GentleAlarm:ringing");
            wakeLock.acquire(MAX_RING_MILLIS + 15_000L);
            AlarmReceiver.releaseHandoff();
            state.setRinging(true);
            state.note("正在轻柔唤醒");
            player.start(this, state.fadeSeconds(), state.ceiling(), message -> {
                state.note(message);
                stopSelf();
            });
            handler.postDelayed(() -> {
                state.note("响铃已达 10 分钟，自动停止");
                stopSelf();
            }, MAX_RING_MILLIS);
        } catch (RuntimeException error) {
            state.note("启动响铃失败：" + error.getMessage());
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private PendingIntent action(String action, int id) {
        return PendingIntent.getBroadcast(this, id,
                new Intent(this, ActionReceiver.class).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override public void onDestroy() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        player.stop();
        AlarmReceiver.releaseHandoff();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        AlarmState state = new AlarmState(this);
        state.setRinging(false);
        if ("正在轻柔唤醒".equals(state.note())) state.note("已停止响铃");
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}

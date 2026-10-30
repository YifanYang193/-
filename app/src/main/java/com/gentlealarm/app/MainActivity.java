package com.gentlealarm.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int[] FADE_SECONDS = {10, 30, 60, 120, 180};
    private final FadingPlayer preview = new FadingPlayer();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private AlarmState state;
    private TimePicker time;
    private Spinner fade;
    private SeekBar ceiling;
    private TextView status;
    private TextView permissions;
    private TextView volumeLabel;
    private Button previewButton;
    private boolean previewing;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = new AlarmState(this);
        AlarmService.createChannel(this);
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.BACKGROUND);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        int padding = Ui.dp(this, 22);
        page.setPadding(padding, padding, padding, padding);
        scroll.addView(page);
        Ui.text(this, page, "轻柔闹钟", 22, Ui.ACCENT);
        Ui.text(this, page, "让早晨，轻一点。", 30, Color.WHITE);
        Ui.text(this, page, "平时安心锁屏，到点才开始轻柔唤醒。", 15, Ui.MUTED);

        LinearLayout permissionCard = Ui.card(this, page);
        Ui.title(this, permissionCard, "响铃准备");
        permissions = Ui.text(this, permissionCard, "", 14, Ui.MUTED);
        Ui.button(this, permissionCard, "允许闹钟和提醒", false, view -> exactPermission());
        Ui.button(this, permissionCard, "允许通知 / 通知设置", false, view -> notificationPermission());
        Ui.button(this, permissionCard, "锁屏全屏提醒设置", false, view -> fullScreenPermission());
        Ui.button(this, permissionCard, "打开系统声音设置", false,
                view -> open(new Intent(Settings.ACTION_SOUND_SETTINGS)));

        LinearLayout editor = Ui.card(this, page);
        Ui.title(this, editor, "想在几点醒来");
        time = new TimePicker(this);
        time.setIs24HourView(true);
        time.setHour(state.hour());
        time.setMinute(state.minute());
        editor.addView(time, new LinearLayout.LayoutParams(-1, -2));
        Ui.text(this, editor, "渐强时长", 16, Color.WHITE);
        fade = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"10 秒", "30 秒", "1 分钟", "2 分钟", "3 分钟"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fade.setAdapter(adapter);
        for (int i = 0; i < FADE_SECONDS.length; i++) {
            if (FADE_SECONDS[i] == state.fadeSeconds()) fade.setSelection(i);
        }
        editor.addView(fade, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        volumeLabel = Ui.text(this, editor, "", 16, Color.WHITE);
        ceiling = new SeekBar(this);
        ceiling.setMax(90);
        ceiling.setProgress(Math.round(state.ceiling() * 100) - 10);
        editor.addView(ceiling, new LinearLayout.LayoutParams(-1, Ui.dp(this, 44)));
        ceiling.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) { updateVolumeLabel(); }
            @Override public void onStartTrackingTouch(SeekBar bar) { stopPreview(); }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        updateVolumeLabel();
        Ui.text(this, editor, "这是应用播放上限，实际响度还受系统“闹钟音量”影响。达到上限后保持，最多响铃 10 分钟。", 13, Ui.MUTED);
        previewButton = Ui.button(this, editor, "试听（30 秒）", false, view -> togglePreview());
        Ui.button(this, editor, "保存并开启一次性闹钟", true,
                view -> confirmSchedule(false));
        Ui.button(this, editor, "1 分钟后锁屏测试", false,
                view -> confirmSchedule(true));
        Ui.text(this, editor, "首版只保留一个待响闹钟。新设置或测试会替换原来的待响闹钟。", 13, Ui.MUTED);

        LinearLayout active = Ui.card(this, page);
        Ui.title(this, active, "当前闹钟");
        status = Ui.text(this, active, "", 17, Color.WHITE);
        Ui.button(this, active, "查看响铃界面", false,
                view -> startActivity(new Intent(this, RingingActivity.class)));
        Ui.button(this, active, "取消待响闹钟", false, view -> {
            AlarmScheduler.cancel(this);
            refresh();
        });
        Ui.button(this, active, "停止当前响铃", false, view -> {
            AlarmActions.stop(this);
            refresh();
        });
        Ui.text(this, page, "自用 Android 版 · 无需登录或联网。首次使用请完成锁屏测试。系统强制停止应用、关机、权限撤销及厂商后台限制可能影响响铃。", 13, Ui.MUTED);
        setContentView(scroll);
        Ui.applyInsets(scroll);
    }

    private float selectedCeiling() { return (ceiling.getProgress() + 10) / 100f; }
    private int selectedFade() { return FADE_SECONDS[fade.getSelectedItemPosition()]; }
    private void updateVolumeLabel() { volumeLabel.setText("播放上限 · " + (ceiling.getProgress() + 10) + "%"); }

    private void confirmSchedule(boolean test) {
        stopPreview();
        if (state.ringing()) { message("请先停止当前响铃"); return; }
        if (!AlarmScheduler.canSchedule(this)) { message("请先允许“闹钟和提醒”权限"); exactPermission(); return; }
        if (!notificationsReady()) { message("请先开启通知，保证可以停止或贪睡"); notificationPermission(); return; }
        if (getSystemService(AudioManager.class).getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
            message("系统闹钟音量为零，请先调高闹钟音量");
            open(new Intent(Settings.ACTION_SOUND_SETTINGS));
            return;
        }
        Runnable save = () -> {
            try {
                long target = test ? System.currentTimeMillis() + 60_000L
                        : AlarmScheduler.nextTime(time.getHour(), time.getMinute());
                AlarmScheduler.schedule(this, target, test);
                state.saveOptions(time.getHour(), time.getMinute(), selectedFade(), selectedCeiling());
                refresh();
                message(test ? "测试已开启，现在可以锁屏" : "已开启，可以正常锁屏休息");
            } catch (RuntimeException error) { message("设置失败：" + error.getMessage()); }
        };
        if (state.enabled()) {
            new AlertDialog.Builder(this).setTitle("替换现有闹钟？")
                    .setMessage("将取消当前待响闹钟，改为本次设置。")
                    .setNegativeButton("保留原闹钟", null)
                    .setPositiveButton("替换", (dialog, which) -> save.run()).show();
        } else save.run();
    }

    private void togglePreview() {
        if (previewing) { stopPreview(); return; }
        if (state.ringing()) { message("当前正在响铃，请先停止"); return; }
        previewing = true;
        previewButton.setText("停止试听");
        preview.start(this, selectedFade(), selectedCeiling(), error -> {
            stopPreview();
            message(error);
        });
        handler.postDelayed(this::stopPreview, 30_000);
    }

    private void stopPreview() {
        handler.removeCallbacksAndMessages(null);
        preview.stop();
        previewing = false;
        if (previewButton != null) previewButton.setText("试听（30 秒）");
    }

    private boolean notificationsReady() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = manager.getNotificationChannel(AlarmService.CHANNEL);
        return manager.areNotificationsEnabled() && (channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
    }

    private void exactPermission() {
        if (Build.VERSION.SDK_INT >= 31 && !AlarmScheduler.canSchedule(this)) {
            open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName())));
        } else message("闹钟和提醒权限已就绪");
    }

    private void notificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            boolean asked = getPreferences(MODE_PRIVATE).getBoolean("notificationAsked", false);
            if (!asked || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                getPreferences(MODE_PRIVATE).edit().putBoolean("notificationAsked", true).apply();
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 40);
            } else open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        } else if (!getSystemService(NotificationManager.class).areNotificationsEnabled()) {
            open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        } else open(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, AlarmService.CHANNEL));
    }

    private void fullScreenPermission() {
        if (Build.VERSION.SDK_INT >= 34) {
            open(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + getPackageName())));
        } else message("本系统通过高优先级闹钟通知提供锁屏提醒");
    }

    private void open(Intent intent) {
        try { startActivity(intent); }
        catch (RuntimeException error) {
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))); }
            catch (RuntimeException ignored) { message("请手动到系统设置查看此应用的权限"); }
        }
    }

    private void refresh() {
        if (state.ringing() && !AlarmService.running) {
            state.setRinging(false);
            state.note("上次响铃进程已结束，请重新测试");
        }
        boolean exact = AlarmScheduler.canSchedule(this);
        boolean fullScreen = Build.VERSION.SDK_INT < 34 || getSystemService(NotificationManager.class).canUseFullScreenIntent();
        AudioManager audio = getSystemService(AudioManager.class);
        String volume = audio.getStreamVolume(AudioManager.STREAM_ALARM) + "/" + audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        permissions.setText("定时权限：" + (exact ? "已允许" : "待开启")
                + "\n通知：" + (notificationsReady() ? "已开启" : "待开启")
                + "\n锁屏全屏：" + (fullScreen ? "可用（以系统实际表现为准）" : "未允许，仍可通过通知操作")
                + "\n系统闹钟音量：" + volume);
        String next = state.enabled() ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
                .format(new Date(state.scheduledAt())) : "没有待响闹钟";
        status.setText((state.ringing() ? "正在响铃\n" : "") + next + "\n" + state.note()
                + (state.enabled() && !exact ? "\n权限未开启，当前不能保证按时响铃" : ""));
    }

    @Override protected void onResume() {
        super.onResume();
        try { AlarmScheduler.restore(this, false); }
        catch (RuntimeException error) { state.note("重新注册失败：" + error.getMessage()); }
        refresh();
    }

    @Override protected void onStop() { stopPreview(); super.onStop(); }
    private void message(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }
}

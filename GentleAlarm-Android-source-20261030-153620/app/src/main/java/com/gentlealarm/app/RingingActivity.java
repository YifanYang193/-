package com.gentlealarm.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;

public final class RingingActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private AlarmState state;
    private TextView clock;
    private final Runnable update = new Runnable() {
        @Override public void run() {
            if (!state.ringing()) { finish(); return; }
            clock.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
            handler.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = new AlarmState(this);
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true); }
        else getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28));
        page.setBackgroundColor(Ui.BACKGROUND);
        Ui.text(this, page, "早安，慢慢醒来", 30, Ui.ACCENT).setGravity(Gravity.CENTER);
        clock = Ui.text(this, page, "", 58, Color.WHITE);
        clock.setGravity(Gravity.CENTER);
        Ui.text(this, page, "声音逐渐增强，到达上限后保持。", 16, Ui.MUTED).setGravity(Gravity.CENTER);
        Ui.button(this, page, "我醒了 · 停止", true, view -> {
            AlarmActions.stop(this);
            finish();
        });
        Ui.button(this, page, "再睡 5 分钟", false, view -> {
            try { AlarmActions.snooze(this); finish(); }
            catch (RuntimeException error) {
                Toast.makeText(this, "贪睡失败：" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        setContentView(page);
    }

    @Override protected void onResume() { super.onResume(); handler.post(update); }
    @Override protected void onPause() { handler.removeCallbacks(update); super.onPause(); }
}

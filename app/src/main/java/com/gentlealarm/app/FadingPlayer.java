package com.gentlealarm.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.function.Consumer;

/** Loops a constant-level tone; gain is independent of the playback loop position. */
final class FadingPlayer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private AudioManager audioManager;
    private AudioFocusRequest focus;
    private long started;
    private int fade;
    private float ceiling;
    private Consumer<String> onFailure;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (player == null) return;
            double progress = Math.min(1, (SystemClock.elapsedRealtime() - started) / (fade * 1000.0));
            float volume = FadeEnvelope.gain(progress, ceiling);
            player.setVolume(volume, volume);
            handler.postDelayed(this, 100);
        }
    };

    void start(Context context, int fadeSeconds, float maxVolume, Consumer<String> failure) {
        stop();
        onFailure = failure;
        fade = Math.max(1, Math.min(300, fadeSeconds));
        ceiling = Math.max(.1f, Math.min(1f, maxVolume));
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        audioManager = context.getSystemService(AudioManager.class);
        focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener(change -> {
                    if (change < 0 && player != null) fail("响铃被来电或其他音频打断，请检查手机");
                }, handler).build();
        if (audioManager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            fail("未取得音频播放权限，可能正在通话");
            return;
        }
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(attributes);
            try (android.content.res.AssetFileDescriptor asset = context.getResources().openRawResourceFd(R.raw.gentle_loop)) {
                player.setDataSource(asset.getFileDescriptor(), asset.getStartOffset(), asset.getLength());
            }
            player.setLooping(true);
            player.setOnErrorListener((media, what, extra) -> {
                fail("音频播放失败（" + what + "/" + extra + "）");
                return true;
            });
            player.prepare();
            player.setVolume(.01f, .01f);
            started = SystemClock.elapsedRealtime();
            player.start();
            handler.post(tick);
        } catch (Exception error) { fail("无法播放铃声：" + error.getMessage()); }
    }

    private void fail(String message) {
        Consumer<String> callback = onFailure;
        stop();
        if (callback != null) callback.accept(message);
    }

    void stop() {
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
        if (audioManager != null && focus != null) audioManager.abandonAudioFocusRequest(focus);
        focus = null;
        onFailure = null;
    }
}

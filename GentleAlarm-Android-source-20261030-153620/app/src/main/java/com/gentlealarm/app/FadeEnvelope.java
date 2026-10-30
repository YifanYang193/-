package com.gentlealarm.app;

/** Gain depends on elapsed ramp time, never on the audio file loop position. */
final class FadeEnvelope {
    private FadeEnvelope() { }

    static float gain(double progress, float ceiling) {
        double t = Math.max(0, Math.min(1, progress));
        double smooth = t * t * (3 - 2 * t);
        return (float) (0.01 * Math.pow(ceiling / 0.01, smooth));
    }
}

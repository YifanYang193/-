package com.gentlealarm.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int BACKGROUND = Color.rgb(20, 26, 38);
    static final int ACCENT = Color.rgb(246, 182, 109);
    static final int MUTED = Color.rgb(177, 185, 200);

    static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    static TextView text(Activity activity, LinearLayout parent, String value, int size, int color) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(activity, 5), 0, dp(activity, 8));
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    static void title(Activity activity, LinearLayout parent, String value) {
        text(activity, parent, value, 19, Color.WHITE).setTypeface(null, Typeface.BOLD);
    }

    static LinearLayout card(Activity activity, LinearLayout parent) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, 18);
        card.setPadding(padding, padding, padding, padding);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(32, 40, 55));
        background.setCornerRadius(dp(activity, 22));
        card.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(activity, 18);
        parent.addView(card, params);
        return card;
    }

    static Button button(Activity activity, LinearLayout parent, String label, boolean primary, View.OnClickListener listener) {
        Button button = new Button(activity);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(primary ? BACKGROUND : ACCENT);
        button.setTextSize(16);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(primary ? ACCENT : Color.rgb(46, 55, 72));
        shape.setCornerRadius(dp(activity, 14));
        button.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(activity, 52));
        params.topMargin = dp(activity, 10);
        parent.addView(button, params);
        button.setOnClickListener(listener);
        return button;
    }

    static void applyInsets(View root) {
        // Insets are applied once to the outer container, including Android 15 edge-to-edge.
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        root.requestApplyInsets();
    }
}

package com.shubhpower.ev;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private static final String PREFS = "shubh_power_prefs";
    private static final String ONBOARDED = "onboarded";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(245, 248, 246));
        root.setFitsSystemWindows(true);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(32), dp(32), dp(32), dp(32));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(28));
        bg.setStroke(dp(1), Color.argb(18, 16, 35, 29));
        card.setBackground(bg);

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.leftMargin = dp(24);
        cardParams.rightMargin = dp(24);
        cardParams.gravity = Gravity.CENTER;

        TextView bolt = new TextView(this);
        bolt.setText("⚡");
        bolt.setTextSize(60f);
        bolt.setTextColor(Color.rgb(22, 162, 103));
        bolt.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Shubh Power");
        title.setTextSize(30f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(16, 35, 29));
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Charging made simple");
        subtitle.setTextSize(16f);
        subtitle.setTextColor(Color.rgb(78, 92, 87));
        subtitle.setGravity(Gravity.CENTER);

        ProgressBar progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.topMargin = dp(4);

        card.addView(bolt);
        card.addView(title);
        card.addView(subtitle);
        card.addView(progressBar, progressParams);

        root.addView(card, cardParams);
        setContentView(root);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, 900L);
    }

    private void routeNext() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Class<?> next = prefs.getBoolean(ONBOARDED, false)
                ? MainActivity.class
                : OnboardingActivity.class;
        startActivity(new Intent(this, next));
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

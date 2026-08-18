package com.shubhpower.ev;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OnboardingActivity extends Activity {
    private static final String PREFS = "shubh_power_prefs";
    private static final String ONBOARDED = "onboarded";

    private static final String[] TITLES = {
            "Find Chargers Near You",
            "Charge Smarter",
            "Pay & Go",
            "Plan Every Trip"
    };

    private static final String[] BODIES = {
            "Locate nearby EV charging stations and check charger availability easily.",
            "Explore charging options, plan your journey, and access useful EV charging tools.",
            "Manage charging sessions and payments directly from your Shubh Power app.",
            "Save stations, compare reliability, and head out with confidence."
    };

    private int step = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        render();
    }

    private void render() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 248, 246));
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setMinimumHeight(getResources().getDisplayMetrics().heightPixels);
        root.setGravity(Gravity.CENTER_VERTICAL);

        TextView skip = label("Skip", 16f, Color.rgb(16, 35, 29), Typeface.BOLD);
        skip.setGravity(Gravity.END);
        skip.setOnClickListener(v -> completeOnboarding());
        root.addView(skip, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(20), dp(28), dp(20), dp(28));
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        heroParams.topMargin = dp(12);

        GradientDrawable heroBg = new GradientDrawable();
        heroBg.setColor(Color.WHITE);
        heroBg.setCornerRadius(dp(28));
        heroBg.setStroke(dp(1), Color.argb(20, 16, 35, 29));
        hero.setBackground(heroBg);

        TextView bolt = new TextView(this);
        bolt.setText("⚡");
        bolt.setTextSize(76f);
        bolt.setTextColor(Color.rgb(22, 162, 103));
        bolt.setGravity(Gravity.CENTER);

        TextView pageTag = label("Step " + (step + 1) + " of " + TITLES.length, 14f, Color.rgb(22, 162, 103), Typeface.BOLD);
        pageTag.setPadding(0, dp(10), 0, 0);

        TextView title = label(TITLES[step], 30f, Color.rgb(16, 35, 29), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, 0);

        TextView body = label(BODIES[step], 17f, Color.rgb(78, 92, 87), Typeface.NORMAL);
        body.setGravity(Gravity.CENTER);
        body.setPadding(dp(8), dp(14), dp(8), 0);
        body.setLineSpacing(0f, 1.15f);

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, dp(24), 0, 0);
        for (int i = 0; i < TITLES.length; i++) {
            TextView dot = new TextView(this);
            dot.setText(i == step ? "●" : "○");
            dot.setTextSize(18f);
            dot.setTextColor(i == step ? Color.rgb(22, 162, 103) : Color.argb(130, 78, 92, 87));
            dot.setPadding(dp(4), 0, dp(4), 0);
            dots.addView(dot);
        }

        hero.addView(bolt);
        hero.addView(pageTag);
        hero.addView(title);
        hero.addView(body);
        hero.addView(dots);
        root.addView(hero, heroParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(18), 0, 0);

        Button skipButton = secondaryButton("Skip");
        skipButton.setOnClickListener(v -> completeOnboarding());

        Button nextButton = primaryButton(step == TITLES.length - 1 ? "Get Started" : "Next");
        nextButton.setOnClickListener(v -> {
            if (step < TITLES.length - 1) {
                step++;
                render();
            } else {
                completeOnboarding();
            }
        });

        LinearLayout.LayoutParams skipParams = new LinearLayout.LayoutParams(0, dp(54), 1f);
        skipParams.rightMargin = dp(8);
        actions.addView(skipButton, skipParams);
        actions.addView(nextButton, new LinearLayout.LayoutParams(0, dp(54), 1f));
        root.addView(actions);

        scrollView.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(scrollView);
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackgroundColor(Color.rgb(22, 162, 103));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextColor(Color.rgb(16, 35, 29));
        button.setTextSize(16f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.argb(45, 16, 35, 29));
        button.setBackground(bg);
        return button;
    }

    private TextView label(String text, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.defaultFromStyle(style));
        return view;
    }

    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(ONBOARDED, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

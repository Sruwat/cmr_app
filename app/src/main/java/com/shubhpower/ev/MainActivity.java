package com.shubhpower.ev;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://shubh-power-ev-ui.shankranand332.chatgpt.site/";
    private static final String PREFS = "shubh_power_prefs";
    private static final String ONBOARDED = "onboarded";
    private WebView webView;
    private int step = 0;

    private final String[] titles = {
            "Welcome to Shubh Power",
            "Find reliable charging nearby",
            "Charge, pay and travel confidently"
    };

    private final String[] bodies = {
            "Discover EV chargers, plan journeys and keep charging simple from one app.",
            "Use the live map, station search and reliability filters to choose the right charging stop.",
            "Scan chargers, use QueuePass, manage payments and open Shubh Rescue when you need urgent help."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(ONBOARDED, false)) {
            showWebApp();
        } else {
            showOnboarding();
        }
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setLineSpacing(0, 1.12f);
        if (bold) v.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return v;
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }

    private void showOnboarding() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(245, 248, 246));

        TextView skip = text("Skip", 16, Color.rgb(16, 35, 29), true);
        skip.setGravity(Gravity.END);
        skip.setPadding(0, dp(8), 0, dp(8));
        skip.setOnClickListener(v -> finishOnboarding());
        root.addView(skip, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout hero = new FrameLayout(this);
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        heroParams.topMargin = dp(18);
        root.addView(hero, heroParams);

        TextView bolt = text("⚡", 76, Color.rgb(22, 162, 103), false);
        bolt.setGravity(Gravity.CENTER);
        hero.addView(bolt, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = text(titles[step], 30, Color.rgb(16, 35, 29), true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView body = text(bodies[step], 17, Color.rgb(78, 92, 87), false);
        body.setGravity(Gravity.CENTER_HORIZONTAL);
        body.setPadding(dp(4), dp(14), dp(4), dp(20));
        root.addView(body, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView dots = text(step == 0 ? "●  ○  ○" : step == 1 ? "○  ●  ○" : "○  ○  ●", 18, Color.rgb(22, 162, 103), true);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, 0, 0, dp(18));
        root.addView(dots, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button next = new Button(this);
        next.setAllCaps(false);
        next.setText(step == titles.length - 1 ? "Get Started" : "Continue");
        next.setTextSize(17);
        next.setTextColor(Color.WHITE);
        next.setBackgroundColor(Color.rgb(22, 162, 103));
        next.setPadding(dp(12), dp(12), dp(12), dp(12));
        next.setOnClickListener(v -> {
            if (step < titles.length - 1) {
                step++;
                showOnboarding();
            } else {
                finishOnboarding();
            }
        });
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        nextParams.bottomMargin = dp(8);
        root.addView(next, nextParams);

        setContentView(root);
    }

    private void finishOnboarding() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(ONBOARDED, true).apply();
        requestLocationIfNeeded();
        showWebApp();
    }

    private void requestLocationIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
        }
    }

    private void showWebApp() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUserAgentString(s.getUserAgentString() + " ShubhPowerAndroid/1.0");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && host.endsWith("chatgpt.site")) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                boolean granted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                callback.invoke(origin, granted, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.loadUrl(APP_URL);
        setContentView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}

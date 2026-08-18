package com.shubhpower.ev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://shubh-power-ev-ui.shankranand332.chatgpt.site/";

    private enum Tab {
        HOME, MAP, SCAN, PAY, PROFILE
    }

    private enum SortMode {
        RELIABLE, FASTEST, CHEAPEST
    }

    private final List<Station> allStations = new ArrayList<>();
    private final List<Station> visibleStations = new ArrayList<>();

    private Tab currentTab = Tab.HOME;
    private SortMode sortMode = SortMode.RELIABLE;
    private String searchQuery = "";

    private FrameLayout root;
    private ScrollView scrollView;
    private LinearLayout bodyContainer;
    private FrameLayout navHost;
    private Station selectedStation;
    private TextView searchResultText;
    private String scanStatusMessage = "Ready to scan a charger.";
    private String payStatusMessage = "Last session: Noida Sec-18 · Completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seedStations();
        applyFilters();
        buildUi();
        render();
    }

    private void seedStations() {
        allStations.add(new Station("Statiq MLCP Noida Sec-18 Charging Hub",
                "Near DLF Mall of India, Sector 18, Noida",
                1.2, 60, 18.25, "4/6 available", 96, 0.68f, 0.58f, Color.parseColor("#7C4DFF")));
        allStations.add(new Station("Delhi Central EV Point",
                "Connaught Place, New Delhi",
                3.8, 120, 21.00, "8/10 available", 94, 0.45f, 0.42f, Color.parseColor("#1E88E5")));
        allStations.add(new Station("FastCharge Ghaziabad",
                "NH-24, Ghaziabad",
                5.1, 80, 16.50, "6/7 available", 91, 0.79f, 0.35f, Color.parseColor("#00ACC1")));
        allStations.add(new Station("Banthla Community Charge",
                "Banthla, Greater Noida",
                6.7, 50, 15.25, "3/4 available", 88, 0.31f, 0.50f, Color.parseColor("#EC407A")));
        allStations.add(new Station("Greater Noida Express Bay",
                "Knowledge Park III, Greater Noida",
                8.2, 150, 23.50, "2/5 available", 93, 0.72f, 0.67f, Color.parseColor("#2E7D32")));
        allStations.add(new Station("Yamuna View Rapid",
                "Noida Extension, Gautam Buddha Nagar",
                9.0, 100, 19.90, "5/8 available", 90, 0.56f, 0.74f, Color.parseColor("#FB8C00")));
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#F6F8FC"));
        root.setFitsSystemWindows(true);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        bodyContainer = new LinearLayout(this);
        bodyContainer.setOrientation(LinearLayout.VERTICAL);
        bodyContainer.setPadding(dp(18), dp(16), dp(18), dp(132));

        scrollView.addView(bodyContainer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        navHost = new FrameLayout(this);
        root.addView(navHost, bottomNavParams());
        setContentView(root);
    }

    private FrameLayout.LayoutParams bottomNavParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        params.leftMargin = dp(14);
        params.rightMargin = dp(14);
        params.bottomMargin = dp(12);
        return params;
    }

    private void render() {
        bodyContainer.removeAllViews();
        navHost.removeAllViews();
        navHost.addView(buildBottomNav(), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        bodyContainer.addView(buildTopBar());
        bodyContainer.addView(space(16));
        bodyContainer.addView(buildSectionHeading());
        bodyContainer.addView(space(14));

        if (currentTab == Tab.HOME || currentTab == Tab.MAP) {
            bodyContainer.addView(buildSearchPanel());
            bodyContainer.addView(space(12));
            bodyContainer.addView(buildSortChips());
            bodyContainer.addView(space(16));
            bodyContainer.addView(buildMapCard());
            bodyContainer.addView(space(16));
            bodyContainer.addView(buildResultsHeader());
            bodyContainer.addView(space(10));
            bodyContainer.addView(buildStationList());
        } else if (currentTab == Tab.SCAN) {
            bodyContainer.addView(buildScanPanel());
        } else if (currentTab == Tab.PAY) {
            bodyContainer.addView(buildPayPanel());
        } else if (currentTab == Tab.PROFILE) {
            bodyContainer.addView(buildProfilePanel());
        }
    }

    private View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout leftGroup = new LinearLayout(this);
        leftGroup.setOrientation(LinearLayout.HORIZONTAL);
        leftGroup.setGravity(Gravity.CENTER_VERTICAL);

        leftGroup.addView(toolbarButton("≡", false, () -> toast("Menu opened")));
        leftGroup.addView(spaceView(dp(10), 0));
        leftGroup.addView(toolbarButton("‹", false, () -> onBackPressed()));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(46), dp(46));
        logoParams.leftMargin = dp(10);
        logoParams.rightMargin = dp(10);
        logo.setLayoutParams(logoParams);

        LinearLayout rightGroup = new LinearLayout(this);
        rightGroup.setOrientation(LinearLayout.HORIZONTAL);
        rightGroup.setGravity(Gravity.CENTER_VERTICAL);
        rightGroup.addView(greetingText());
        rightGroup.addView(spaceView(dp(10), 0));
        rightGroup.addView(circleAvatar("S"));
        rightGroup.addView(spaceView(dp(10), 0));
        rightGroup.addView(toolbarButton("🔔", false, () -> toast("Notifications")));

        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        bar.addView(leftGroup, leftParams);
        bar.addView(logo, centerParams);
        bar.addView(rightGroup, rightParams);
        return bar;
    }

    private TextView greetingText() {
        TextView view = new TextView(this);
        view.setText("Good aft...");
        view.setTextSize(16f);
        view.setTextColor(Color.parseColor("#506078"));
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setMaxLines(1);
        return view;
    }

    private View circleAvatar(String letter) {
        TextView avatar = new TextView(this);
        avatar.setText(letter);
        avatar.setTextColor(Color.WHITE);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextSize(18f);
        avatar.setPadding(0, 0, 0, 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1E6FBF"));
        bg.setShape(GradientDrawable.OVAL);
        avatar.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(46), dp(46));
        avatar.setLayoutParams(params);
        return avatar;
    }

    private View toolbarButton(String icon, boolean blue, Runnable action) {
        TextView button = new TextView(this);
        button.setText(icon);
        button.setTextSize(22f);
        button.setTextColor(blue ? Color.parseColor("#0F6CCF") : Color.parseColor("#1B2436"));
        button.setGravity(Gravity.CENTER);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(0, 0, 0, dp(2));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor("#DDE5F0"));
        button.setBackground(bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(54), dp(54));
        button.setLayoutParams(params);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private View buildSectionHeading() {
        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(sectionTitle());
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#1E3350"));
        title.setLineSpacing(0f, 0.95f);

        TextView subtitle = new TextView(this);
        subtitle.setText(sectionSubtitle());
        subtitle.setTextSize(14f);
        subtitle.setTextColor(Color.parseColor("#6B778A"));
        subtitle.setPadding(0, dp(4), 0, 0);

        holder.addView(title);
        holder.addView(subtitle);
        return holder;
    }

    private String sectionTitle() {
        if (currentTab == Tab.SCAN) {
            return "Scan to start charging.";
        }
        if (currentTab == Tab.PAY) {
            return "Pay and manage sessions.";
        }
        if (currentTab == Tab.PROFILE) {
            return "Your account and settings.";
        }
        if (currentTab == Tab.MAP) {
            return "Explore chargers nearby.";
        }
        return "Charge with confidence.";
    }

    private String sectionSubtitle() {
        if (currentTab == Tab.SCAN) {
            return "Scan a station QR or simulate a session preview without leaving the app.";
        }
        if (currentTab == Tab.PAY) {
            return "Review charging history, wallet balance, and payment shortcuts.";
        }
        if (currentTab == Tab.PROFILE) {
            return "Open support, preferences, and the live web experience if you need it.";
        }
        if (currentTab == Tab.MAP) {
            return "Search stations, sort results, and pick the charger that fits the trip.";
        }
        return "Search stations, compare reliability, and keep the main dashboard easy to scan.";
    }

    private View buildSearchPanel() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        final EditText searchInput = new EditText(this);
        searchInput.setText(searchQuery);
        searchInput.setHint("Search station, place or network");
        searchInput.setTextSize(16f);
        searchInput.setSingleLine(true);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setPadding(dp(18), dp(16), dp(18), dp(16));
        searchInput.setTextColor(Color.parseColor("#1D2B3E"));
        searchInput.setHintTextColor(Color.parseColor("#7E8A9A"));
        searchInput.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#DDE5F0"), dp(18)));
        searchInput.setLayoutParams(new LinearLayout.LayoutParams(0, dp(58), 1f));
        searchInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            applySearch(searchInput.getText().toString());
            return true;
        });

        View filter = toolbarButton("⚙", true, () -> showSortDialog());
        LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        filterParams.leftMargin = dp(10);
        filter.setLayoutParams(filterParams);

        row.addView(searchInput);
        row.addView(filter);
        return row;
    }

    private View buildSortChips() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(sortChip("Most reliable", SortMode.RELIABLE, "🛡"));
        row.addView(spaceView(dp(10), 0));
        row.addView(sortChip("Fastest", SortMode.FASTEST, "⚡"));
        row.addView(spaceView(dp(10), 0));
        row.addView(sortChip("Cheapest", SortMode.CHEAPEST, "₹"));
        return row;
    }

    private View sortChip(String label, SortMode mode, String icon) {
        boolean active = sortMode == mode;
        Button chip = new Button(this);
        chip.setAllCaps(false);
        chip.setText(icon + " " + label);
        chip.setTextSize(15f);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setPadding(dp(16), dp(12), dp(16), dp(12));
        chip.setTextColor(active ? Color.parseColor("#0F6CCF") : Color.parseColor("#4B5870"));
        chip.setBackground(buildRoundedStroke(
                active ? Color.parseColor("#D7ECFF") : Color.WHITE,
                active ? Color.parseColor("#26A0E8") : Color.parseColor("#DCE4EE"),
                dp(18)
        ));
        chip.setOnClickListener(v -> {
            sortMode = mode;
            applyFilters();
            render();
        });
        return chip;
    }

    private View buildMapCard() {
        FrameLayout card = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#EEF4FB"));
        bg.setCornerRadius(dp(28));
        card.setBackground(bg);
        card.setMinimumHeight(dp(currentTab == Tab.MAP ? 430 : 380));

        MapCanvasView canvasView = new MapCanvasView(this);
        canvasView.setStations(visibleStations);
        canvasView.setFocusedStation(selectedStation);
        card.addView(canvasView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView bubble = buildInfoBubble();
        FrameLayout.LayoutParams bubbleParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubbleParams.leftMargin = dp(14);
        bubbleParams.topMargin = dp(14);
        card.addView(bubble, bubbleParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.addView(roundActionButton("⌖", () -> toast("Center on current area")));
        actions.addView(space(dp(12)));
        actions.addView(roundActionButton("☰", () -> showSortDialog()));
        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.gravity = Gravity.END | Gravity.TOP;
        actionParams.rightMargin = dp(14);
        actionParams.topMargin = dp(80);
        card.addView(actions, actionParams);

        View selectedCard = buildSelectedStationCard();
        FrameLayout.LayoutParams selectedParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedParams.gravity = Gravity.BOTTOM;
        selectedParams.leftMargin = dp(14);
        selectedParams.rightMargin = dp(14);
        selectedParams.bottomMargin = dp(14);
        card.addView(selectedCard, selectedParams);

        return card;
    }

    private TextView buildInfoBubble() {
        TextView bubble = new TextView(this);
        bubble.setText("10 verified locations\nDemo availability");
        bubble.setTextSize(14f);
        bubble.setTextColor(Color.parseColor("#24324A"));
        bubble.setTypeface(Typeface.DEFAULT_BOLD);
        bubble.setLineSpacing(0f, 1.1f);
        bubble.setPadding(dp(16), dp(14), dp(16), dp(14));
        bubble.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#DCE4EE"), dp(18)));
        return bubble;
    }

    private View roundActionButton(String icon, Runnable action) {
        TextView button = new TextView(this);
        button.setText(icon);
        button.setTextSize(22f);
        button.setTextColor(Color.parseColor("#0F6CCF"));
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, dp(2));
        button.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#DCE4EE"), dp(16)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(54), dp(54));
        button.setLayoutParams(params);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private View buildSelectedStationCard() {
        Station station = selectedStation != null ? selectedStation : firstVisible();
        if (station == null) {
            return emptyStateCard("No stations found", "Try a different search or clear the filter.");
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(24)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        badge.setText(station.initial());
        badge.setTextSize(18f);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(stationBadgeBackground(station.color));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        badgeParams.rightMargin = dp(12);
        header.addView(badge, badgeParams);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        TextView name = titleText(station.name, 17f);
        TextView address = bodyText(station.address + " · " + formatDistance(station.distanceKm), 13f);
        textColumn.addView(name);
        textColumn.addView(address);
        header.addView(textColumn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = circleArrow();
        header.addView(arrow);
        card.addView(header);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, dp(12), 0, 0);

        stats.addView(statPill("⚡ " + station.powerKw + " kW", Color.parseColor("#24324A")));
        stats.addView(spaceView(dp(8), 0));
        stats.addView(statPill("₹" + formatPrice(station.pricePerKwh) + "/kWh", Color.parseColor("#24324A")));
        stats.addView(spaceView(dp(8), 0));
        stats.addView(statPill(station.score + " ChargeSure", Color.parseColor("#149B6B")));

        card.addView(stats);

        TextView availability = new TextView(this);
        availability.setText("✓ " + station.availability);
        availability.setTextColor(Color.parseColor("#149B6B"));
        availability.setTypeface(Typeface.DEFAULT_BOLD);
        availability.setTextSize(14f);
        availability.setPadding(0, dp(12), 0, 0);
        card.addView(availability);

        card.setOnClickListener(v -> {
            selectedStation = station;
            render();
        });
        arrow.setOnClickListener(v -> {
            selectedStation = station;
            toast(station.name + " selected");
        });
        return card;
    }

    private TextView circleArrow() {
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(30f);
        arrow.setTypeface(Typeface.DEFAULT_BOLD);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Color.parseColor("#0F6CCF"));
        arrow.setBackground(buildRoundedStroke(Color.parseColor("#EAF4FF"), Color.parseColor("#D5E8FB"), dp(20)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
        arrow.setLayoutParams(params);
        return arrow;
    }

    private TextView statPill(String text, int color) {
        TextView pill = new TextView(this);
        pill.setText(text);
        pill.setTextColor(color);
        pill.setTypeface(Typeface.DEFAULT_BOLD);
        pill.setTextSize(13f);
        pill.setPadding(dp(12), dp(8), dp(12), dp(8));
        pill.setBackground(buildRoundedStroke(Color.parseColor("#F7FAFD"), Color.parseColor("#E3EAF3"), dp(16)));
        return pill;
    }

    private View stationRow(Station station) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(20)));

        TextView badge = new TextView(this);
        badge.setText(station.initial());
        badge.setTextColor(Color.WHITE);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextSize(18f);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(stationBadgeBackground(station.color));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        badgeParams.rightMargin = dp(12);
        card.addView(badge, badgeParams);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        TextView name = titleText(station.name, 16f);
        TextView details = bodyText(station.address + " · " + formatDistance(station.distanceKm), 13f);
        TextView meta = bodyText(station.powerKw + " kW  ·  ₹" + formatPrice(station.pricePerKwh) + "/kWh  ·  " + station.availability, 13f);
        column.addView(name);
        column.addView(details);
        column.addView(meta);
        card.addView(column, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView score = new TextView(this);
        score.setText(station.score + "\nChargeSure");
        score.setTextColor(Color.parseColor("#149B6B"));
        score.setTypeface(Typeface.DEFAULT_BOLD);
        score.setTextSize(13f);
        score.setGravity(Gravity.CENTER);
        score.setPadding(dp(8), dp(8), dp(8), dp(8));
        score.setBackground(buildRoundedStroke(Color.parseColor("#E7FBF2"), Color.parseColor("#D0F5E4"), dp(16)));
        card.addView(score);

        card.setOnClickListener(v -> {
            selectedStation = station;
            render();
        });
        return card;
    }

    private View historyRow(String title, String meta) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView dot = new TextView(this);
        dot.setText("•");
        dot.setTextSize(24f);
        dot.setTextColor(Color.parseColor("#0F6CCF"));
        dot.setTypeface(Typeface.DEFAULT_BOLD);
        dot.setPadding(0, 0, dp(10), 0);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(titleText(title, 15f));
        column.addView(bodyText(meta, 13f));

        row.addView(dot);
        row.addView(column);
        return row;
    }

    private View profileAction(String title, String subtitle, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        row.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(20)));
        row.addView(titleText(title, 16f));
        row.addView(bodyText(subtitle, 13f));
        row.setOnClickListener(v -> action.run());
        return row;
    }

    private View buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(12), dp(12), dp(12), dp(12));
        nav.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(28)));
        nav.setElevation(dp(8));

        nav.addView(navItem("⌂", "Home", Tab.HOME));
        nav.addView(navItem("⌖", "Map", Tab.MAP));
        nav.addView(navItem("▣", "Scan", Tab.SCAN));
        nav.addView(navItem("▤", "Pay", Tab.PAY));
        nav.addView(navItem("◌", "Profile", Tab.PROFILE));
        return nav;
    }

    private View navItem(String icon, String label, Tab tab) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(10), dp(8), dp(10), dp(8));

        boolean active = currentTab == tab;

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(24f);
        iconView.setTypeface(Typeface.DEFAULT_BOLD);
        iconView.setTextColor(active ? Color.parseColor("#0F6CCF") : Color.parseColor("#6B778A"));
        iconView.setGravity(Gravity.CENTER);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(12f);
        labelView.setTypeface(Typeface.DEFAULT_BOLD);
        labelView.setTextColor(active ? Color.parseColor("#0F6CCF") : Color.parseColor("#6B778A"));
        labelView.setGravity(Gravity.CENTER);

        item.addView(iconView);
        item.addView(labelView);
        item.setOnClickListener(v -> {
            currentTab = tab;
            render();
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        item.setLayoutParams(params);
        return item;
    }

    private View buildSelectedStationCardCompact() {
        Station station = selectedStation != null ? selectedStation : firstVisible();
        if (station == null) {
            return emptyStateCard("No station selected", "Search for a charger to fill this card.");
        }
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(18)));
        card.addView(titleText(station.name, 15f));
        card.addView(bodyText(station.address, 13f));
        card.addView(bodyText(station.powerKw + " kW · ₹" + formatPrice(station.pricePerKwh) + "/kWh", 13f));
        return card;
    }

    private View buildStationList() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        if (visibleStations.isEmpty()) {
            list.addView(emptyStateCard("Nothing matches yet", "Try a broader search or switch sort mode."));
            return list;
        }

        int count = currentTab == Tab.MAP ? visibleStations.size() : Math.min(visibleStations.size(), 3);
        for (int i = 0; i < count; i++) {
            Station station = visibleStations.get(i);
            list.addView(stationRow(station));
            if (i < count - 1) {
                list.addView(space(dp(10)));
            }
        }
        return list;
    }

    private View buildScanPreviewCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(22)));
        card.addView(titleText("Selected station preview", 16f));
        card.addView(space(dp(8)));
        card.addView(buildSelectedStationCardCompact());
        return card;
    }

    private View buildMapPreviewCard() {
        LinearLayout panel = actionPanel();
        panel.addView(buildMapCard());
        panel.addView(space(dp(14)));
        panel.addView(buildSelectedStationCardCompact());
        panel.addView(space(dp(14)));
        panel.addView(buildStationList());
        return panel;
    }

    private View buildResultsHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        searchResultText = new TextView(this);
        searchResultText.setText(resultSummary());
        searchResultText.setTextSize(14f);
        searchResultText.setTypeface(Typeface.DEFAULT_BOLD);
        searchResultText.setTextColor(Color.parseColor("#55657E"));

        TextView clear = new TextView(this);
        clear.setText("Clear");
        clear.setTextSize(14f);
        clear.setTextColor(Color.parseColor("#0F6CCF"));
        clear.setTypeface(Typeface.DEFAULT_BOLD);
        clear.setOnClickListener(v -> {
            searchQuery = "";
            sortMode = SortMode.RELIABLE;
            applyFilters();
            render();
        });

        row.addView(searchResultText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(clear);
        return row;
    }

    private View buildScanPanel() {
        LinearLayout panel = actionPanel();
        panel.addView(buildScanHeader());
        panel.addView(space(dp(16)));

        TextView qr = new TextView(this);
        qr.setText("▣");
        qr.setTextSize(74f);
        qr.setTypeface(Typeface.DEFAULT_BOLD);
        qr.setGravity(Gravity.CENTER);
        qr.setTextColor(Color.parseColor("#0F6CCF"));

        TextView desc = bodyText("Tap start to simulate a QR scan and load the selected charger session.", 15f);
        desc.setGravity(Gravity.CENTER);

        final TextView scanStatusText = new TextView(this);
        scanStatusText.setText("Ready to scan a charger.");
        scanStatusText.setTextColor(Color.parseColor("#24324A"));
        scanStatusText.setTypeface(Typeface.DEFAULT_BOLD);
        scanStatusText.setGravity(Gravity.CENTER);

        Button start = primaryActionButton("Start Scan", () -> {
            Station station = selectedStation != null ? selectedStation : firstVisible();
            if (station == null) {
                scanStatusMessage = "No charger selected yet.";
                scanStatusText.setText(scanStatusMessage);
                toast("Pick a charger first");
            } else {
                scanStatusMessage = "Scan matched: " + station.name;
                scanStatusText.setText(scanStatusMessage);
                toast("Scan complete");
            }
        });

        Button simulate = secondaryActionButton("Simulate another QR", () -> {
            Station station = chooseNextStation(selectedStation);
            selectedStation = station;
            scanStatusMessage = "Simulated QR matched: " + (station != null ? station.name : "None");
            scanStatusText.setText(scanStatusMessage);
            toast("QR switched");
            render();
        });

        panel.addView(qr);
        panel.addView(space(dp(10)));
        panel.addView(desc);
        panel.addView(space(dp(12)));
        panel.addView(scanStatusText);
        panel.addView(space(dp(16)));
        panel.addView(start);
        panel.addView(space(dp(10)));
        panel.addView(simulate);
        panel.addView(space(dp(16)));
        panel.addView(buildScanPreviewCard());
        return panel;
    }

    private View buildScanHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(14), dp(14), dp(14));
        header.setBackground(buildRoundedStroke(Color.parseColor("#EAF4FF"), Color.parseColor("#D5E8FB"), dp(20)));

        TextView icon = new TextView(this);
        icon.setText("⚡");
        icon.setTextSize(22f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setTextColor(Color.parseColor("#0F6CCF"));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(12), 0, 0, 0);
        text.addView(titleText("Quick scan mode", 16f));
        text.addView(bodyText("Useful for charging QR codes and station session previews.", 13f));

        header.addView(icon);
        header.addView(text);
        return header;
    }

    private View buildPayPanel() {
        LinearLayout panel = actionPanel();
        panel.addView(buildPayHeader());
        panel.addView(space(dp(14)));

        LinearLayout wallet = new LinearLayout(this);
        wallet.setOrientation(LinearLayout.VERTICAL);
        wallet.setPadding(dp(16), dp(16), dp(16), dp(16));
        wallet.setBackground(buildRoundedStroke(Color.parseColor("#0F6CCF"), Color.parseColor("#0F6CCF"), dp(24)));

        TextView balanceLabel = new TextView(this);
        balanceLabel.setText("Wallet balance");
        balanceLabel.setTextColor(Color.parseColor("#D7E9FF"));
        balanceLabel.setTypeface(Typeface.DEFAULT_BOLD);

        TextView balance = new TextView(this);
        balance.setText("₹1,250.00");
        balance.setTextColor(Color.WHITE);
        balance.setTypeface(Typeface.DEFAULT_BOLD);
        balance.setTextSize(32f);
        balance.setPadding(0, dp(8), 0, dp(2));

        TextView payStatusText = new TextView(this);
        payStatusText.setText(payStatusMessage);
        payStatusText.setTextColor(Color.WHITE);

        wallet.addView(balanceLabel);
        wallet.addView(balance);
        wallet.addView(payStatusText);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(pillButton("Pay now", false, null, () -> toast("Opening payment flow")));
        actions.addView(spaceView(dp(10), 0));
        actions.addView(pillButton("History", false, null, () -> toast("Showing session history")));
        actions.addView(spaceView(dp(10), 0));
        actions.addView(pillButton("Add money", true, null, () -> {
            toast("Wallet topped up");
            payStatusMessage = "Wallet topped up successfully";
            render();
        }));

        panel.addView(wallet);
        panel.addView(space(dp(14)));
        panel.addView(actions);
        panel.addView(space(dp(14)));
        panel.addView(buildPayHistoryCard());
        return panel;
    }

    private View buildPayHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(14), dp(14), dp(14));
        header.setBackground(buildRoundedStroke(Color.parseColor("#EAF4FF"), Color.parseColor("#D5E8FB"), dp(20)));

        TextView icon = new TextView(this);
        icon.setText("₹");
        icon.setTextSize(22f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setTextColor(Color.parseColor("#0F6CCF"));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(12), 0, 0, 0);
        text.addView(titleText("Simple checkout", 16f));
        text.addView(bodyText("Review the session and top up your wallet instantly.", 13f));

        header.addView(icon);
        header.addView(text);
        return header;
    }

    private View buildPayHistoryCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(22)));

        card.addView(titleText("Recent sessions", 16f));
        card.addView(space(dp(10)));
        card.addView(historyRow("Noida Sec-18", "₹218.00 · 24 min"));
        card.addView(space(dp(10)));
        card.addView(historyRow("Connaught Place", "₹342.50 · 39 min"));
        card.addView(space(dp(10)));
        card.addView(historyRow("Ghaziabad Rapid Hub", "₹162.75 · 19 min"));
        return card;
    }

    private View historyRow(String title, String meta) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView dot = new TextView(this);
        dot.setText("•");
        dot.setTextSize(24f);
        dot.setTextColor(Color.parseColor("#0F6CCF"));
        dot.setTypeface(Typeface.DEFAULT_BOLD);
        dot.setPadding(0, 0, dp(10), 0);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(titleText(title, 15f));
        column.addView(bodyText(meta, 13f));

        row.addView(dot);
        row.addView(column);
        return row;
    }

    private View buildProfilePanel() {
        LinearLayout panel = actionPanel();
        panel.addView(buildProfileHeader());
        panel.addView(space(dp(14)));

        LinearLayout profile = new LinearLayout(this);
        profile.setOrientation(LinearLayout.HORIZONTAL);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(dp(16), dp(16), dp(16), dp(16));
        profile.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(24)));

        profile.addView(circleAvatar("S"));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14), 0, 0, 0);
        info.addView(titleText("Shubh Power Client", 17f));
        info.addView(bodyText("shubhpower@example.com", 13f));
        profile.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        panel.addView(profile);
        panel.addView(space(dp(14)));
        panel.addView(profileAction("Open live UI", "Launch the website in your browser", this::openLiveUi));
        panel.addView(space(dp(10)));
        panel.addView(profileAction("Support", "Help, account and charging guidance", () -> toast("Support opened")));
        panel.addView(space(dp(10)));
        panel.addView(profileAction("Preferences", "Update app preferences and theme", () -> toast("Preferences opened")));
        panel.addView(space(dp(14)));
        panel.addView(buildProfileStats());
        return panel;
    }

    private View buildProfileHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(14), dp(14), dp(14));
        header.setBackground(buildRoundedStroke(Color.parseColor("#EAF4FF"), Color.parseColor("#D5E8FB"), dp(20)));

        TextView icon = new TextView(this);
        icon.setText("S");
        icon.setTextSize(22f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setTextColor(Color.parseColor("#0F6CCF"));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(12), 0, 0, 0);
        text.addView(titleText("Personal dashboard", 16f));
        text.addView(bodyText("Keep support, preferences and live UI access in one place.", 13f));

        header.addView(icon);
        header.addView(text);
        return header;
    }

    private View buildProfileStats() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(22)));
        card.addView(titleText("App info", 16f));
        card.addView(space(dp(10)));
        card.addView(bodyText("Version 1.0.0", 14f));
        card.addView(bodyText("Standalone offline dashboard + live web access", 14f));
        card.addView(bodyText("First-launch onboarding stored locally", 14f));
        return card;
    }

    private View profileAction(String title, String subtitle, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        row.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(20)));
        row.addView(titleText(title, 16f));
        row.addView(bodyText(subtitle, 13f));
        row.setOnClickListener(v -> action.run());
        return row;
    }

    private View actionPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(14), dp(14), dp(14));
        panel.setBackground(buildRoundedStroke(Color.TRANSPARENT, Color.TRANSPARENT, dp(0)));
        return panel;
    }

    private View emptyStateCard(String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(20)));
        card.addView(titleText(title, 16f));
        card.addView(bodyText(subtitle, 13f));
        return card;
    }

    private View buildMapCard() {
        FrameLayout card = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#EEF4FB"));
        bg.setCornerRadius(dp(28));
        card.setBackground(bg);
        card.setMinimumHeight(dp(currentTab == Tab.MAP ? 430 : 380));

        MapCanvasView canvasView = new MapCanvasView(this);
        canvasView.setStations(visibleStations);
        canvasView.setFocusedStation(selectedStation);
        card.addView(canvasView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView bubble = buildInfoBubble();
        FrameLayout.LayoutParams bubbleParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubbleParams.leftMargin = dp(14);
        bubbleParams.topMargin = dp(14);
        card.addView(bubble, bubbleParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.addView(roundActionButton("⌖", () -> toast("Center on current area")));
        actions.addView(space(dp(12)));
        actions.addView(roundActionButton("☰", () -> showSortDialog()));
        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.gravity = Gravity.END | Gravity.TOP;
        actionParams.rightMargin = dp(14);
        actionParams.topMargin = dp(80);
        card.addView(actions, actionParams);

        View selectedCard = buildSelectedStationCard();
        FrameLayout.LayoutParams selectedParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectedParams.gravity = Gravity.BOTTOM;
        selectedParams.leftMargin = dp(14);
        selectedParams.rightMargin = dp(14);
        selectedParams.bottomMargin = dp(14);
        card.addView(selectedCard, selectedParams);

        return card;
    }

    private View buildInfoBubble() {
        TextView bubble = new TextView(this);
        bubble.setText("10 verified locations\nDemo availability");
        bubble.setTextSize(14f);
        bubble.setTextColor(Color.parseColor("#24324A"));
        bubble.setTypeface(Typeface.DEFAULT_BOLD);
        bubble.setLineSpacing(0f, 1.1f);
        bubble.setPadding(dp(16), dp(14), dp(16), dp(14));
        bubble.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#DCE4EE"), dp(18)));
        return bubble;
    }

    private View buildSelectedStationCard() {
        Station station = selectedStation != null ? selectedStation : firstVisible();
        if (station == null) {
            return emptyStateCard("No stations found", "Try a different search or clear the filter.");
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(24)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        badge.setText(station.initial());
        badge.setTextSize(18f);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(stationBadgeBackground(station.color));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        badgeParams.rightMargin = dp(12);
        header.addView(badge, badgeParams);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        TextView name = titleText(station.name, 17f);
        TextView address = bodyText(station.address + " · " + formatDistance(station.distanceKm), 13f);
        textColumn.addView(name);
        textColumn.addView(address);
        header.addView(textColumn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = circleArrow();
        header.addView(arrow);
        card.addView(header);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, dp(12), 0, 0);
        stats.addView(statPill("⚡ " + station.powerKw + " kW", Color.parseColor("#24324A")));
        stats.addView(spaceView(dp(8), 0));
        stats.addView(statPill("₹" + formatPrice(station.pricePerKwh) + "/kWh", Color.parseColor("#24324A")));
        stats.addView(spaceView(dp(8), 0));
        stats.addView(statPill(station.score + " ChargeSure", Color.parseColor("#149B6B")));
        card.addView(stats);

        TextView availability = new TextView(this);
        availability.setText("✓ " + station.availability);
        availability.setTextColor(Color.parseColor("#149B6B"));
        availability.setTypeface(Typeface.DEFAULT_BOLD);
        availability.setTextSize(14f);
        availability.setPadding(0, dp(12), 0, 0);
        card.addView(availability);

        card.setOnClickListener(v -> {
            selectedStation = station;
            render();
        });
        arrow.setOnClickListener(v -> {
            selectedStation = station;
            toast(station.name + " selected");
        });
        return card;
    }

    private View buildSelectedStationCardCompact() {
        Station station = selectedStation != null ? selectedStation : firstVisible();
        if (station == null) {
            return emptyStateCard("No station selected", "Search for a charger to fill this card.");
        }
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#E3EAF3"), dp(18)));
        card.addView(titleText(station.name, 15f));
        card.addView(bodyText(station.address, 13f));
        card.addView(bodyText(station.powerKw + " kW · ₹" + formatPrice(station.pricePerKwh) + "/kWh", 13f));
        return card;
    }

    private TextView circleArrow() {
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(30f);
        arrow.setTypeface(Typeface.DEFAULT_BOLD);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Color.parseColor("#0F6CCF"));
        arrow.setBackground(buildRoundedStroke(Color.parseColor("#EAF4FF"), Color.parseColor("#D5E8FB"), dp(20)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(44));
        arrow.setLayoutParams(params);
        return arrow;
    }

    private TextView statPill(String text, int color) {
        TextView pill = new TextView(this);
        pill.setText(text);
        pill.setTextColor(color);
        pill.setTypeface(Typeface.DEFAULT_BOLD);
        pill.setTextSize(13f);
        pill.setPadding(dp(12), dp(8), dp(12), dp(8));
        pill.setBackground(buildRoundedStroke(Color.parseColor("#F7FAFD"), Color.parseColor("#E3EAF3"), dp(16)));
        return pill;
    }

    private TextView titleText(String text, float size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.parseColor("#1D2B3E"));
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    private TextView bodyText(String text, float size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.parseColor("#66778D"));
        view.setTextSize(size);
        return view;
    }

    private void showSortDialog() {
        String[] items = new String[]{"Most reliable", "Fastest", "Cheapest"};
        int checked = sortMode == SortMode.RELIABLE ? 0 : sortMode == SortMode.FASTEST ? 1 : 2;
        new AlertDialog.Builder(this)
                .setTitle("Sort stations")
                .setSingleChoiceItems(items, checked, (dialog, which) -> {
                    sortMode = which == 0 ? SortMode.RELIABLE : which == 1 ? SortMode.FASTEST : SortMode.CHEAPEST;
                    applyFilters();
                    render();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applySearch(String query) {
        searchQuery = query == null ? "" : query.trim();
        applyFilters();
        render();
    }

    private void applyFilters() {
        visibleStations.clear();
        String normalized = searchQuery.toLowerCase(Locale.US);
        for (Station station : allStations) {
            if (normalized.isEmpty() || station.matches(normalized)) {
                visibleStations.add(station);
            }
        }

        Comparator<Station> comparator;
        if (sortMode == SortMode.FASTEST) {
            comparator = (a, b) -> Double.compare(b.powerKw, a.powerKw);
        } else if (sortMode == SortMode.CHEAPEST) {
            comparator = (a, b) -> Double.compare(a.pricePerKwh, b.pricePerKwh);
        } else {
            comparator = (a, b) -> Integer.compare(b.score, a.score);
        }
        Collections.sort(visibleStations, comparator);

        if (selectedStation == null || !visibleStations.contains(selectedStation)) {
            selectedStation = visibleStations.isEmpty() ? null : visibleStations.get(0);
        }
    }

    private String resultSummary() {
        if (visibleStations.isEmpty()) {
            return "0 stations found";
        }
        return visibleStations.size() + " stations found";
    }

    private Station firstVisible() {
        return visibleStations.isEmpty() ? null : visibleStations.get(0);
    }

    private Station chooseNextStation(Station current) {
        if (visibleStations.isEmpty()) {
            return null;
        }
        if (current == null) {
            return visibleStations.get(0);
        }
        int index = visibleStations.indexOf(current);
        if (index < 0 || index + 1 >= visibleStations.size()) {
            return visibleStations.get(0);
        }
        return visibleStations.get(index + 1);
    }

    private void openLiveUi() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(APP_URL)));
        } catch (ActivityNotFoundException e) {
            toast("No browser available");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private GradientDrawable buildRoundedStroke(int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private GradientDrawable stationBadgeBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(18));
        return drawable;
    }

    private Button pillButton(String text, boolean active, String icon, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(icon == null ? text : icon + " " + text);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextSize(15f);
        button.setPadding(dp(16), dp(12), dp(16), dp(12));
        button.setTextColor(active ? Color.parseColor("#0F6CCF") : Color.parseColor("#4B5870"));
        button.setBackground(buildRoundedStroke(
                active ? Color.parseColor("#D7ECFF") : Color.WHITE,
                active ? Color.parseColor("#26A0E8") : Color.parseColor("#DCE4EE"),
                18
        ));
        if (action != null) {
            button.setOnClickListener(v -> action.run());
        }
        return button;
    }

    private Button primaryActionButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextSize(16f);
        button.setTextColor(Color.WHITE);
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setBackground(buildRoundedStroke(Color.parseColor("#0F6CCF"), Color.parseColor("#0F6CCF"), 18));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private Button secondaryActionButton(String text, Runnable action) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextSize(16f);
        button.setTextColor(Color.parseColor("#1D2B3E"));
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setBackground(buildRoundedStroke(Color.WHITE, Color.parseColor("#DCE4EE"), 18));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private LinearLayout actionPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(0), dp(0), dp(0), dp(0));
        return panel;
    }

    private View space(int size) {
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size));
        return spacer;
    }

    private View spaceView(int width, int height) {
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return spacer;
    }

    private String formatDistance(double distanceKm) {
        return String.format(Locale.US, "%.1f km", distanceKm);
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f", price);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (currentTab != Tab.HOME) {
            currentTab = Tab.HOME;
            render();
            return;
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchQuery = "";
            applyFilters();
            render();
            return;
        }
        super.onBackPressed();
    }

    private static class Station {
        final String name;
        final String address;
        final double distanceKm;
        final int powerKw;
        final double pricePerKwh;
        final String availability;
        final int score;
        final float x;
        final float y;
        final int color;

        Station(String name, String address, double distanceKm, int powerKw, double pricePerKwh,
                String availability, int score, float x, float y, int color) {
            this.name = name;
            this.address = address;
            this.distanceKm = distanceKm;
            this.powerKw = powerKw;
            this.pricePerKwh = pricePerKwh;
            this.availability = availability;
            this.score = score;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        boolean matches(String query) {
            String combined = (name + " " + address + " " + availability).toLowerCase(Locale.US);
            return combined.contains(query);
        }

        String initial() {
            return name.isEmpty() ? "S" : String.valueOf(name.charAt(0));
        }
    }

    private static class MapCanvasView extends View {
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint roadPaintSoft = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint cityPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint markerRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path roadPath1 = new Path();
        private final Path roadPath2 = new Path();
        private final Path roadPath3 = new Path();
        private List<Station> stations = Collections.emptyList();
        private Station focusedStation;

        MapCanvasView(MainActivity context) {
            super(context);
            backgroundPaint.setColor(Color.parseColor("#F5F8FC"));
            roadPaint.setColor(Color.parseColor("#E3E9F1"));
            roadPaint.setStyle(Paint.Style.STROKE);
            roadPaint.setStrokeWidth(context.dp(4));
            roadPaint.setStrokeCap(Paint.Cap.ROUND);
            roadPaintSoft.setColor(Color.parseColor("#F0F4F8"));
            roadPaintSoft.setStyle(Paint.Style.STROKE);
            roadPaintSoft.setStrokeWidth(context.dp(2));
            roadPaintSoft.setStrokeCap(Paint.Cap.ROUND);
            cityPaint.setColor(Color.parseColor("#6A778B"));
            cityPaint.setTextSize(context.dp(18));
            cityPaint.setFakeBoldText(true);
            markerRingPaint.setStyle(Paint.Style.STROKE);
            markerRingPaint.setStrokeWidth(context.dp(6));
            textPaint.setTextSize(context.dp(13));
            textPaint.setFakeBoldText(true);
        }

        void setStations(List<Station> stations) {
            this.stations = stations == null ? Collections.emptyList() : stations;
            invalidate();
        }

        void setFocusedStation(Station focusedStation) {
            this.focusedStation = focusedStation;
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            roadPath1.reset();
            roadPath1.moveTo(w * 0.10f, h * 0.28f);
            roadPath1.cubicTo(w * 0.30f, h * 0.18f, w * 0.50f, h * 0.46f, w * 0.78f, h * 0.34f);

            roadPath2.reset();
            roadPath2.moveTo(w * 0.18f, h * 0.78f);
            roadPath2.cubicTo(w * 0.38f, h * 0.64f, w * 0.60f, h * 0.90f, w * 0.88f, h * 0.74f);

            roadPath3.reset();
            roadPath3.moveTo(w * 0.06f, h * 0.55f);
            roadPath3.cubicTo(w * 0.24f, h * 0.50f, w * 0.42f, h * 0.66f, w * 0.62f, h * 0.55f);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.parseColor("#F5F8FC"));
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), getWidth() * 0.05f, getHeight() * 0.05f, backgroundPaint);
            canvas.drawPath(roadPath1, roadPaint);
            canvas.drawPath(roadPath2, roadPaintSoft);
            canvas.drawPath(roadPath3, roadPaintSoft);

            drawCity(canvas, "Delhi", 0.20f, 0.41f);
            drawCity(canvas, "New Delhi", 0.18f, 0.52f);
            drawCity(canvas, "Noida", 0.72f, 0.61f);
            drawCity(canvas, "Ghaziabad", 0.83f, 0.42f);

            for (Station station : stations) {
                drawMarker(canvas, station);
            }
        }

        private void drawCity(Canvas canvas, String label, float xFactor, float yFactor) {
            float x = getWidth() * xFactor;
            float y = getHeight() * yFactor;
            canvas.drawText(label, x, y, cityPaint);
        }

        private void drawMarker(Canvas canvas, Station station) {
            float x = getWidth() * station.x;
            float y = getHeight() * station.y;
            boolean focused = station == focusedStation;
            int baseColor = focused ? Color.parseColor("#0F6CCF") : station.color;
            markerPaint.setColor(baseColor);
            markerRingPaint.setColor(adjustAlpha(baseColor, 0.24f));
            canvas.drawCircle(x, y, focused ? getWidth() * 0.035f : getWidth() * 0.025f, markerRingPaint);
            canvas.drawCircle(x, y, focused ? getWidth() * 0.020f : getWidth() * 0.015f, markerPaint);
            textPaint.setColor(Color.WHITE);
            String text = String.valueOf(Math.min(station.score / 20, 9));
            float textWidth = textPaint.measureText(text);
            canvas.drawText(text, x - textWidth / 2f, y + textPaint.getTextSize() / 3f, textPaint);
        }

        private int adjustAlpha(int color, float alpha) {
            int a = Math.round(Color.alpha(color) * alpha);
            return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
        }
    }
}

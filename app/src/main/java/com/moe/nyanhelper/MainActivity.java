package com.moe.nyanhelper;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // 顶部状态
    private TextView tvFloat, tvAccess, tvService;
    private Button btnToggle, btnAccess, btnRefresh;
    private LinearLayout bgRoot;

    // 三个 Tab
    private LinearLayout tabFunc, tabSet, tabTheme;
    private View pageFunc, pageSet, pageTheme;

    // 功能页开关
    private Switch swAddNya, swMe, swYou;

    // 设置页开关（雪花/流星互斥）
    private Switch swSnow, swMeteor;

    // 主题页三个色卡
    private View cardPink, cardPurple, cardMint;
    private View dotPink, dotPurple, dotMint;

    private static final int[] THEME_BG = {
            0xFFFCE7F0, // 粉樱
            0xFF2A1A4A, // 星空紫
            0xFFE3F6EF, // 薄荷
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupStatus();
        setupTabs();
        setupFunctionSwitches();
        setupSettingSwitches();
        setupThemeCards();
        applyTheme(false);
        update();
    }

    private void bindViews() {
        bgRoot = findViewById(R.id.bgRoot);
        tvFloat = findViewById(R.id.tvFloatStatus);
        tvAccess = findViewById(R.id.tvAccessStatus);
        tvService = findViewById(R.id.tvServiceStatus);
        btnToggle = findViewById(R.id.btnFloatToggle);
        btnAccess = findViewById(R.id.btnAccessOpen);
        btnRefresh = findViewById(R.id.btnRefresh);

        tabFunc = findViewById(R.id.tabFunc);
        tabSet = findViewById(R.id.tabSet);
        tabTheme = findViewById(R.id.tabTheme);
        pageFunc = findViewById(R.id.pageFunc);
        pageSet = findViewById(R.id.pageSet);
        pageTheme = findViewById(R.id.pageTheme);

        swAddNya = findViewById(R.id.swAddNya);
        swMe = findViewById(R.id.swMe);
        swYou = findViewById(R.id.swYou);

        swSnow = findViewById(R.id.swSnow);
        swMeteor = findViewById(R.id.swMeteor);

        cardPink = findViewById(R.id.cardPink);
        cardPurple = findViewById(R.id.cardPurple);
        cardMint = findViewById(R.id.cardMint);
        dotPink = findViewById(R.id.dotPink);
        dotPurple = findViewById(R.id.dotPurple);
        dotMint = findViewById(R.id.dotMint);
    }

    private void setupStatus() {
        btnToggle.setOnClickListener(v -> toggle());
        btnAccess.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnRefresh.setOnClickListener(v -> update());
        ImageView avatar = findViewById(R.id.avatar);
        if (avatar != null) avatar.setImageResource(R.drawable.nyan_avatar);
    }

    // ============ Tab 切换 ============
    private void setupTabs() {
        tabFunc.setOnClickListener(v -> showPage(0));
        tabSet.setOnClickListener(v -> showPage(1));
        tabTheme.setOnClickListener(v -> showPage(2));
        showPage(0);
    }

    private void showPage(int idx) {
        pageFunc.setVisibility(idx == 0 ? View.VISIBLE : View.GONE);
        pageSet.setVisibility(idx == 1 ? View.VISIBLE : View.GONE);
        pageTheme.setVisibility(idx == 2 ? View.VISIBLE : View.GONE);
        setTabActive(tabFunc, idx == 0);
        setTabActive(tabSet, idx == 1);
        setTabActive(tabTheme, idx == 2);
    }

    private void setTabActive(LinearLayout tab, boolean active) {
        int pink = ContextCompat.getColor(this, R.color.primary);
        int gray = 0xFFB0A0B0;
        TextView label = (TextView) tab.getChildAt(1);
        if (label != null) label.setTextColor(active ? pink : gray);
        tab.setAlpha(active ? 1f : 0.7f);
    }

    // ============ 功能页开关 ============
    private void setupFunctionSwitches() {
        swAddNya.setChecked(NyanConfig.isAddNya(this));
        swMe.setChecked(NyanConfig.isMe(this));
        swYou.setChecked(NyanConfig.isYou(this));
        swAddNya.setOnCheckedChangeListener((b, c) -> NyanConfig.setAddNya(this, c));
        swMe.setOnCheckedChangeListener((b, c) -> NyanConfig.setMe(this, c));
        swYou.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setYou(this, c);
            if (c) Toast.makeText(this, "以后「你」会变成「主人」喵~", Toast.LENGTH_SHORT).show();
        });
    }

    // ============ 设置页开关（互斥） ============
    private void setupSettingSwitches() {
        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));

        swSnow.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                NyanConfig.setMeteor(this, false);
                swMeteor.setChecked(false);
            }
            NyanConfig.setSnow(this, checked);
            sendEffectUpdate();
        });

        swMeteor.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                NyanConfig.setSnow(this, false);
                swSnow.setChecked(false);
            }
            NyanConfig.setMeteor(this, checked);
            sendEffectUpdate();
        });
    }

    private void sendEffectUpdate() {
        // 通知悬浮球刷新动效状态
        Intent it = new Intent(this, FloatWindowService.class);
        it.setAction("UPDATE_EFFECT");
        startService(it);
    }

    // ============ 主题页 ============
    private void setupThemeCards() {
        cardPink.setOnClickListener(v -> setTheme(0));
        cardPurple.setOnClickListener(v -> setTheme(1));
        cardMint.setOnClickListener(v -> setTheme(2));
    }

    private void setTheme(int t) {
        NyanConfig.setTheme(this, t);
        applyTheme(true);
    }

    private void applyTheme(boolean animate) {
        int bg = THEME_BG[NyanConfig.getTheme(this)];
        if (animate) {
            ValueAnimator a = ValueAnimator.ofArgb(bgRoot.getBackgroundColor(), bg);
            a.setDuration(400);
            a.addUpdateListener(anim -> bgRoot.setBackgroundColor((int) anim.getAnimatedValue()));
            a.start();
        } else {
            bgRoot.setBackgroundColor(bg);
        }
        // 更新选中圆点
        dotPink.setAlpha(NyanConfig.getTheme(this) == 0 ? 1f : 0.25f);
        dotPurple.setAlpha(NyanConfig.getTheme(this) == 1 ? 1f : 0.25f);
        dotMint.setAlpha(NyanConfig.getTheme(this) == 2 ? 1f : 0.25f);

        // 星空紫主题文字改浅色
        int textColor = (NyanConfig.getTheme(this) == 1) ? 0xFFFFFFFF : 0xFF3A2A3A;
        int subColor = (NyanConfig.getTheme(this) == 1) ? 0xFFD0C0E0 : 0xFF8A7A9A;
        ((TextView) findViewById(R.id.slogan)).setTextColor(subColor);
        ((TextView) findViewById(R.id.tvFloatStatus)).setTextColor(textColor);
        ((TextView) findViewById(R.id.tvAccessStatus)).setTextColor(textColor);
        ((TextView) findViewById(R.id.tvServiceStatus)).setTextColor(textColor);
    }

    // ============ 状态刷新 ============
    private void update() {
        boolean hasFloat = Settings.canDrawOverlays(this);
        tvFloat.setText(hasFloat ? "✅ 悬浮窗权限已开启" : "❌ 悬浮窗权限未开启");

        boolean hasAccess = false;
        for (android.accessibilityservice.AccessibilityServiceInfo info :
                ((android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))
                        .getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getId() != null && info.getId().contains("nyanhelper")) hasAccess = true;
        }
        tvAccess.setText(hasAccess ? "✅ 无障碍服务已开启" : "❌ 无障碍服务未开启");

        boolean running = getSharedPreferences("nyan_config", MODE_PRIVATE).getBoolean("float_started", false);
        tvService.setText(running && hasFloat ? "🟢 悬浮窗服务运行中" : "⚪ 悬浮窗服务未运行");
        btnToggle.setText(running && hasFloat ? "关闭悬浮窗" : "开启悬浮窗");
    }

    private void toggle() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "请先授予悬浮窗权限喵~", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, FloatWindowService.class);
        boolean running = getSharedPreferences("nyan_config", MODE_PRIVATE).getBoolean("float_started", false);
        if (running) {
            stopService(intent);
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit().putBoolean("float_started", false).apply();
            Toast.makeText(this, "悬浮窗已关闭喵~", Toast.LENGTH_SHORT).show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit().putBoolean("float_started", true).apply();
            Toast.makeText(this, "悬浮窗已开启喵~", Toast.LENGTH_SHORT).show();
        }
        update();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }
}

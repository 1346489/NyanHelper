package com.moe.nyanhelper;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvFloat, tvAccess, tvService;
    private Button btnToggle, btnAccess, btnRefresh;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        applyTheme();
        update();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_layout);
        ImageView avatar = findViewById(R.id.avatar);
        tvFloat = findViewById(R.id.tvFloatStatus);
        tvAccess = findViewById(R.id.tvAccessStatus);
        tvService = findViewById(R.id.tvServiceStatus);
        btnToggle = findViewById(R.id.btnFloatToggle);
        btnAccess = findViewById(R.id.btnAccessOpen);
        btnRefresh = findViewById(R.id.btnRefresh);

        avatar.setImageResource(R.drawable.avatar_main);

        btnToggle.setOnClickListener(v -> toggle());
        btnAccess.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnRefresh.setOnClickListener(v -> update());
    }

    /** 背景跟随主题三色 */
    private void applyTheme() {
        int s = NyanConfig.themeStartColor(this);
        int c = NyanConfig.themeCenterColor(this);
        int e = NyanConfig.themeEndColor(this);
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{s, c, e});
        d.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        rootLayout.setBackground(d);

        int primary = NyanConfig.themePrimary(this);
        btnToggle.setBackgroundColor(primary);
    }

    private void update() {
        boolean hasFloat = Settings.canDrawOverlays(this);
        tvFloat.setText(hasFloat ? "✅ 悬浮窗权限已开启" : "❌ 悬浮窗权限未开启");

        boolean hasAccess = false;
        android.view.accessibility.AccessibilityManager am =
                (android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am != null) {
            for (android.accessibilityservice.AccessibilityServiceInfo info :
                    am.getEnabledAccessibilityServiceList(
                            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                if (info.getId() != null && info.getId().contains("nyanhelper")) {
                    hasAccess = true;
                    break;
                }
            }
        }
        tvAccess.setText(hasAccess ? "✅ 无障碍服务已开启" : "❌ 无障碍服务未开启");

        boolean running = NyanConfig.isFloatStarted(this);
        tvService.setText(running && hasFloat ? "🟢 悬浮窗服务运行中" : "⚪ 悬浮窗服务未运行");
        btnToggle.setText(running && hasFloat ? "关闭悬浮窗" : "开启悬浮窗");
    }

    private void toggle() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "请先授予悬浮窗权限喵~", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, FloatWindowService.class);
        boolean running = NyanConfig.isFloatStarted(this);
        if (running) {
            stopService(intent);
            NyanConfig.setFloatStarted(this, false);
            Toast.makeText(this, "悬浮窗已关闭喵~", Toast.LENGTH_SHORT).show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            NyanConfig.setFloatStarted(this, true);
            Toast.makeText(this, "悬浮窗已开启喵~", Toast.LENGTH_SHORT).show();
        }
        update();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        update();
    }
}

package com.moe.nyanhelper;

import android.content.Intent;
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

    private LinearLayout rootLayout;
    private TextView tvFloat, tvAccess, tvService;
    private ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        avatar = findViewById(R.id.avatar);
        tvFloat = findViewById(R.id.tvFloatStatus);
        tvAccess = findViewById(R.id.tvAccessStatus);
        tvService = findViewById(R.id.tvServiceStatus);

        findViewById(R.id.btnShowFloat).setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "请先授予悬浮窗权限喵~", Toast.LENGTH_LONG).show();
                return;
            }
            startService(new Intent(this, FloatService.class));
            NyanConfig.setServiceRunning(this, true);
            update();
        });

        findViewById(R.id.btnOpenAccess).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        findViewById(R.id.btnHideFloat).setOnClickListener(v -> {
            stopService(new Intent(this, FloatService.class));
            NyanConfig.setServiceRunning(this, false);
            update();
        });

        findViewById(R.id.btnFeatures).setOnClickListener(v ->
                startActivity(new Intent(this, FeaturesActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btnTheme).setOnClickListener(v ->
                startActivity(new Intent(this, ThemeActivity.class)));

        applyTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        tvFloat.setText(Settings.canDrawOverlays(this)
                ? "✅ 悬浮窗权限已开启" : "❌ 悬浮窗权限未开启");
        tvAccess.setText(isAccessibilityEnabled()
                ? "✅ 无障碍服务已开启" : "❌ 无障碍服务未开启");
        tvService.setText(NyanConfig.isServiceRunning(this)
                ? "🟢 悬浮窗服务运行中" : "⚪ 悬浮窗服务未运行");
    }

    private boolean isAccessibilityEnabled() {
        try {
            android.view.accessibility.AccessibilityManager am =
                    (android.view.accessibility.AccessibilityManager)
                            getSystemService(ACCESSIBILITY_SERVICE);
            for (android.accessibilityservice.AccessibilityServiceInfo info :
                    am.getEnabledAccessibilityServiceList(
                            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                if (info.getId() != null && info.getId().contains("nyanhelper")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void applyTheme() {
        rootLayout.setBackgroundColor(NyanConfig.getThemeColor(this));
        // 深色主题（星空紫）时文字反色
        int textColor = NyanConfig.getTheme(this) == 2 ? 0xFFFFFFFF : 0xFF5A2E4A;
        tvFloat.setTextColor(textColor);
        tvAccess.setTextColor(textColor);
        tvService.setTextColor(textColor);
        ((TextView) findViewById(R.id.title)).setTextColor(textColor);
        ((TextView) findViewById(R.id.subtitle)).setTextColor(textColor);
        avatar.setBackgroundColor(NyanConfig.getTheme(this) == 2 ? 0x33FFFFFF : 0xFFFFFFFF);
    }
}

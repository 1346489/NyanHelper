package com.moe.nyanhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvFloatStatus, tvAccessStatus, tvServiceStatus;
    private Button btnFloatToggle, btnAccessOpen, btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        updateStatus();

        btnFloatToggle.setOnClickListener(v -> toggleFloatWindow());
        btnAccessOpen.setOnClickListener(v -> openAccessibilitySettings());
        btnRefresh.setOnClickListener(v -> updateStatus());
    }

    private void initViews() {
        ImageView avatar = findViewById(R.id.avatar);
        tvFloatStatus = findViewById(R.id.tvFloatStatus);
        tvAccessStatus = findViewById(R.id.tvAccessStatus);
        tvServiceStatus = findViewById(R.id.tvServiceStatus);
        btnFloatToggle = findViewById(R.id.btnFloatToggle);
        btnAccessOpen = findViewById(R.id.btnAccessOpen);
        btnRefresh = findViewById(R.id.btnServiceToggle);

        avatar.setImageResource(R.drawable.avatar);
    }

    private void updateStatus() {
        boolean hasFloat = Settings.canDrawOverlays(this);
        tvFloatStatus.setText(hasFloat ? "✅ 悬浮窗权限已开启" : "❌ 悬浮窗权限未开启");

        boolean hasAccess = isAccessibilityEnabled();
        tvAccessStatus.setText(hasAccess ? "✅ 无障碍服务已开启" : "❌ 无障碍服务未开启");

        boolean floatStarted = getSharedPreferences("nyan_config", MODE_PRIVATE)
                .getBoolean("float_started", false);
        tvServiceStatus.setText(floatStarted && hasFloat
                ? "🟢 悬浮窗服务运行中" : "⚪ 悬浮窗服务未运行");
        btnFloatToggle.setText(floatStarted && hasFloat ? "关闭悬浮窗" : "开启悬浮窗");
    }

    private void toggleFloatWindow() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "请先授予悬浮窗权限喵~", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, FloatWindowService.class);
        boolean currentlyRunning = getSharedPreferences("nyan_config", MODE_PRIVATE)
                .getBoolean("float_started", false);

        if (currentlyRunning) {
            stopService(intent);
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit()
                    .putBoolean("float_started", false).apply();
            Toast.makeText(this, "悬浮窗已关闭喵~", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit()
                    .putBoolean("float_started", true).apply();
            Toast.makeText(this, "悬浮窗已开启喵~", Toast.LENGTH_SHORT).show();
        }
        updateStatus();
    }

    private void openAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private boolean isAccessibilityEnabled() {
        for (android.accessibilityservice.AccessibilityServiceInfo info :
                ((android.view.accessibility.AccessibilityManager)
                        getSystemService(ACCESSIBILITY_SERVICE))
                        .getEnabledAccessibilityServiceList(
                                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getId() != null && info.getId().contains("nyanhelper")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}

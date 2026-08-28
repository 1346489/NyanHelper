package com.moe.nyanhelper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this, findViewById(android.R.id.content));
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnService = findViewById(R.id.btnService);

        findViewById(R.id.btnFeatures).setOnClickListener(
                v -> startActivity(new Intent(this, FeaturesActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(
                v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btnTheme).setOnClickListener(
                v -> startActivity(new Intent(this, ThemeActivity.class)));

        btnService.setOnClickListener(v -> {
            if (!hasOverlayPermission()) {
                requestOverlayPermission();
                Toast.makeText(this, "请开启「显示在其他应用上层」权限", Toast.LENGTH_LONG).show();
                return;
            }
            if (NyanConfig.isServiceRunning(this)) {
                stopService(new Intent(this, FloatWindowService.class));
                NyanConfig.setServiceRunning(this, false);
            } else {
                Intent intent = new Intent(this, FloatWindowService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
                NyanConfig.setServiceRunning(this, true);
            }
            updateServiceButton();
        });

        updateServiceButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceButton();
    }

    private void updateServiceButton() {
        if (tvStatus == null || btnService == null) return;
        if (NyanConfig.isServiceRunning(this)) {
            tvStatus.setText("● 服务运行中");
            tvStatus.setTextColor(0xFF4CAF50);
            btnService.setText("关闭悬浮窗/特效");
        } else {
            tvStatus.setText("○ 服务未运行");
            tvStatus.setTextColor(0xFF999999);
            btnService.setText("启动悬浮窗/特效");
        }
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}

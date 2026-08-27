package com.moe.nyanhelper;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 100;
    private static final int REQUEST_NOTIFICATION = 101;

    private TextView tvStatus;
    private Button btnFloat;
    private Button btnAccessibility;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===== UI 布局 =====
        tvStatus = new TextView(this);
        tvStatus.setText("本喵助手");
        tvStatus.setTextSize(22);
        tvStatus.setGravity(android.view.Gravity.CENTER);

        btnFloat = new Button(this);
        btnFloat.setText("打开悬浮窗权限");
        btnFloat.setOnClickListener(v -> requestOverlay());

        btnAccessibility = new Button(this);
        btnAccessibility.setText("打开无障碍权限");
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnClose = new Button(this);
        btnClose.setText("关闭悬浮窗");
        btnClose.setOnClickListener(v -> {
            stopService(new Intent(this, FloatWindowService.class));
            updateStatus();
        });

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(50, 100, 50, 50);
        layout.addView(tvStatus);
        layout.addView(btnFloat);
        layout.addView(btnAccessibility);
        layout.addView(btnClose);

        setContentView(layout);

        // Android 13+ 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean canOverlay = Settings.canDrawOverlays(this);
        // 无障碍状态检测
        boolean accessibilityEnabled = isAccessibilityEnabled();

        StringBuilder sb = new StringBuilder();
        sb.append("悬浮窗：").append(canOverlay ? "已开启喵" : "未开启").append("\n");
        sb.append("无障碍：").append(accessibilityEnabled ? "已开启喵" : "未开启");
        tvStatus.setText(sb.toString());

        btnFloat.setText(canOverlay ? "悬浮窗已授权" : "打开悬浮窗权限");
    }

    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Exception e) {
            return false;
        }
        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.contains(getPackageName());
            }
        }
        return false;
    }

    private void requestOverlay() {
        if (Settings.canDrawOverlays(this)) {
            // 已有权限，直接启动服务
            startFloatService();
        } else {
            // 跳转系统授权页
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, REQUEST_OVERLAY);
        }
    }

    private void startFloatService() {
        Intent intent = new Intent(this, FloatWindowService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "悬浮窗已启动喵~", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                startFloatService();
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能显示喵", Toast.LENGTH_LONG).show();
            }
        }
    }
}

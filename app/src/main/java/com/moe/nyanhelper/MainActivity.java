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

    private TextView statusText;
    private Button btnFloat, btnAccessibility, btnOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        btnFloat = findViewById(R.id.btnFloat);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnOverlay = findViewById(R.id.btnOverlay);

        btnAccessibility.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        btnOverlay.setOnClickListener(v -> requestOverlayPermission());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean overlay = Settings.canDrawOverlays(this);
        boolean accessibility = isAccessibilityEnabled();
        boolean serviceRunning = isServiceRunning(FloatWindowService.class);

        StringBuilder sb = new StringBuilder();
        sb.append("悬浮窗权限：").append(overlay ? "已授权" : "未授权").append("\n");
        sb.append("无障碍：").append(accessibility ? "已开启喵" : "未开启").append("\n");
        sb.append("悬浮窗服务：").append(serviceRunning ? "运行中" : "未运行");
        statusText.setText(sb.toString());

        if (serviceRunning) {
            btnFloat.setText("关闭悬浮窗");
            btnFloat.setOnClickListener(v -> {
                stopService(new Intent(this, FloatWindowService.class));
                updateStatus();
                Toast.makeText(this, "悬浮窗已关闭喵~", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnFloat.setText("开启悬浮窗");
            btnFloat.setOnClickListener(v -> {
                if (Settings.canDrawOverlays(this)) {
                    startFloatService();
                } else {
                    Toast.makeText(this, "请先授予悬浮窗权限喵~", Toast.LENGTH_SHORT).show();
                    requestOverlayPermission();
                }
            });
        }
    }

    private boolean isAccessibilityEnabled() {
        try {
            int enabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            if (enabled == 1) {
                String services = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                return services != null && services.contains(getPackageName());
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null) {
            for (android.app.ActivityManager.RunningServiceInfo info : am.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(info.service.getClassName())) return true;
            }
        }
        return false;
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY);
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
                Toast.makeText(this, "悬浮窗权限被拒绝了喵 T_T", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

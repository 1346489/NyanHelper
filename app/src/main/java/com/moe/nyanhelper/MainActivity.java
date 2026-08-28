package com.moe.nyanhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnFeatures, btnSettings, btnTheme, btnService;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this, null); // 仅初始化，下面 setContentView 后用 root
        setContentView(R.layout.activity_main);
        ThemeManager.apply(this, findViewById(R.id.root));

        tvStatus = findViewById(R.id.tvStatus);
        btnFeatures = findViewById(R.id.btnFeatures);
        btnSettings = findViewById(R.id.btnSettings);
        btnTheme = findViewById(R.id.btnTheme);
        btnService = findViewById(R.id.btnService);

        btnFeatures.setOnClickListener(v -> startActivity(new Intent(this, FeaturesActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnTheme.setOnClickListener(v -> startActivity(new Intent(this, ThemeActivity.class)));

        btnService.setOnClickListener(v -> {
            if (!hasOverlayPermission()) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
                return;
            }
            if (NyanConfig.isServiceRunning(this)) stopFloat();
            else startFloat();
        });

        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean running = NyanConfig.isServiceRunning(this);
        tvStatus.setText(running ? "● 悬浮窗运行中" : "○ 服务未运行");
        tvStatus.setTextColor(running ? 0xFF4CAF50 : 0xFF999999);
        btnService.setText(running ? "关闭悬浮窗" : "启动悬浮窗");
    }

    private void startFloat() {
        Intent intent = new Intent(this, FloatService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
        // FloatService.onCreate 内会 setServiceRunning(true)
        updateStatus();
    }

    private void stopFloat() {
        stopService(new Intent(this, FloatService.class));
        NyanConfig.setServiceRunning(this, false);
        updateStatus();
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }
}

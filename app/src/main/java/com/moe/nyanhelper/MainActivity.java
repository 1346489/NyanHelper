package com.moe.nyanhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button btnFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        btnFloat = findViewById(R.id.btnFloat);

        btnFloat.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
            boolean on = !Prefs.isFloatOn(this);
            Intent svc = new Intent(this, FloatWindowService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
            Prefs.setFloatOn(this, on);
            refresh();
        });

        findViewById(R.id.btnAccessibility).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        findViewById(R.id.btnOverlay).setOnClickListener(v ->
                startActivity(new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()))));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean floatOk = Settings.canDrawOverlays(this) && Prefs.isFloatOn(this);
        btnFloat.setText(floatOk ? "关闭悬浮窗" : "开启悬浮窗");
        statusText.setText("悬浮窗：" + (floatOk ? "已开启喵" : "未开启喵"));
    }
}

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

    private TextView statusText;
    private Button btnFloat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        btnFloat = findViewById(R.id.btnFloat);

        btnFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    startActivity(new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                    ));
                    return;
                }
                boolean on = !Prefs.isFloatOn(MainActivity.this);
                try {
                    Intent intent = new Intent(MainActivity.this, FloatWindowService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                    Prefs.setFloatOn(MainActivity.this, on);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                refresh();
            }
        });

        findViewById(R.id.btnAccessibility).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        findViewById(R.id.btnOverlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                ));
            }
        });
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

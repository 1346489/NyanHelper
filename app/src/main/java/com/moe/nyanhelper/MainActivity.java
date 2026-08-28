package com.moe.nyanhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status);
        Button btnFloat = findViewById(R.id.btnFloat);
        Button btnAccess = findViewById(R.id.btnAccess);
        Button btnHide = findViewById(R.id.btnHide);

        btnFloat.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatWindowService.class));
                statusText.setText("状态：悬浮喵已显示～");
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "请授予悬浮窗权限喵～", Toast.LENGTH_SHORT).show();
            }
        });

        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnHide.setOnClickListener(v -> {
            stopService(new Intent(this, FloatWindowService.class));
            statusText.setText("状态：待机中～");
        });
    }
}

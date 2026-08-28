package com.moe.nyanhelper;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 应用主题背景
        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.applyTheme(this, rootView);
        }

        tvService = findViewById(R.id.tvService);
        TextView btnFeatures = findViewById(R.id.btnFeatures);
        TextView btnSettings = findViewById(R.id.btnSettings);
        TextView btnTheme = findViewById(R.id.btnTheme);
        TextView btnStart = findViewById(R.id.btnStart);

        updateServiceStatus();

        btnFeatures.setOnClickListener(v -> {
            startActivity(new Intent(this, FeaturesActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        btnTheme.setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });

        btnStart.setOnClickListener(v -> {
            if (!NyanConfig.isServiceRunning(this)) {
                // 跳转到无障碍设置页面
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "请在无障碍设置里找到「本喵助手」并开启", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "本喵助手已在运行中~", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
        // 每次返回刷新主题
        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.applyTheme(this, rootView);
        }
    }

    private void updateServiceStatus() {
        if (tvService != null) {
            tvService.setText(
                NyanConfig.isServiceRunning(this) ? "● 服务运行中" : "○ 服务未运行"
            );
            tvService.setTextColor(
                NyanConfig.isServiceRunning(this) ? 0xFF4CAF50 : 0xFF999999
            );
        }
    }
}

package com.moe.nyanhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this, findViewById(android.R.id.content));
        setContentView(R.layout.activity_settings);

        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.apply(this, rootView);
        }

        Switch swSnow = findViewById(R.id.swSnow);
        Switch swMeteor = findViewById(R.id.swMeteor);

        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));

        swSnow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 开雪花 → 自动关流星（互斥在 NyanConfig 里也做了双保险）
            if (isChecked) {
                NyanConfig.setMeteor(this, false);
                swMeteor.setChecked(false);
            }
            NyanConfig.setSnow(this, isChecked);
            Toast.makeText(this, "雪花：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
            notifyRefresh();
        });

        swMeteor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 开流星 → 自动关雪花
            if (isChecked) {
                NyanConfig.setSnow(this, false);
                swSnow.setChecked(false);
            }
            NyanConfig.setMeteor(this, isChecked);
            Toast.makeText(this, "流星：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
            notifyRefresh();
        });
    }

    /** 通知悬浮窗刷新特效层 */
    private void notifyRefresh() {
        if (NyanConfig.isServiceRunning(this)) {
            startService(new android.content.Intent(this, FloatWindowService.class)
                    .setAction(FloatWindowService.ACTION_REFRESH_EFFECT));
        }
    }
}

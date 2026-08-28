package com.moe.nyanhelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch swSnow, swMeteor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ThemeManager.apply(this, findViewById(R.id.root));

        swSnow = findViewById(R.id.swSnow);
        swMeteor = findViewById(R.id.swMeteor);

        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));

        swSnow.setOnCheckedChangeListener((b, checked) -> {
            if (checked) { NyanConfig.setMeteor(this, false); swMeteor.setChecked(false); }
            NyanConfig.setSnow(this, checked);
            Toast.makeText(this, "雪花特效：" + (checked ? "开" : "关"), Toast.LENGTH_SHORT).show();
            notifyFloatRefresh();
        });

        swMeteor.setOnCheckedChangeListener((b, checked) -> {
            if (checked) { NyanConfig.setSnow(this, false); swSnow.setChecked(false); }
            NyanConfig.setMeteor(this, checked);
            Toast.makeText(this, "流星特效：" + (checked ? "开" : "关"), Toast.LENGTH_SHORT).show();
            notifyFloatRefresh();
        });
    }

    /** 通知 FloatService 重新读取开关并刷新特效层 */
    private void notifyFloatRefresh() {
        if (NyanConfig.isServiceRunning(this)) {
            startService(new Intent(this, FloatService.class).setAction("REFRESH_EFFECT"));
        }
    }
}

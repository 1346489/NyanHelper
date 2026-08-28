package com.moe.nyanhelper;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.applyTheme(this, rootView);
        }

        Switch swSnow = findViewById(R.id.swSnow);
        Switch swMeteor = findViewById(R.id.swMeteor);

        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));

        swSnow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setSnow(this, isChecked);
            swMeteor.setChecked(NyanConfig.isMeteor(this));
            Toast.makeText(this, "雪花：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });

        swMeteor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setMeteor(this, isChecked);
            swSnow.setChecked(NyanConfig.isSnow(this));
            Toast.makeText(this, "流星：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
    }
}

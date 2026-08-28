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
        setContentView(R.layout.activity_settings);

        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.applyTheme(this, rootView);
        }

        Switch swSnow = findViewById(R.id.swSnow);
        Switch swMeteor = findViewById(R.id.swMeteor);
        Switch swReplaceYou = findViewById(R.id.swReplaceYou);
        Switch swReplaceMe = findViewById(R.id.swReplaceMe);

        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));
        swReplaceYou.setChecked(NyanConfig.isReplaceYou(this));
        swReplaceMe.setChecked(NyanConfig.isReplaceMe(this));

        swSnow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                NyanConfig.setMeteor(this, false);
                swMeteor.setChecked(false);
            }
            NyanConfig.setSnow(this, isChecked);
        });

        swMeteor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                NyanConfig.setSnow(this, false);
                swSnow.setChecked(false);
            }
            NyanConfig.setMeteor(this, isChecked);
        });

        swReplaceYou.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setReplaceYou(this, isChecked);
            Toast.makeText(this, "你→主人：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });

        swReplaceMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setReplaceMe(this, isChecked);
            Toast.makeText(this, "我→本喵：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
    }
}

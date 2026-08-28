package com.moe.nyanhelper;

import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch swSnow, swMeteor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        swSnow = findViewById(R.id.swSnow);
        swMeteor = findViewById(R.id.swMeteor);

        refresh();

        swSnow.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setSnow(this, c);
            if (c) NyanConfig.setMeteor(this, false);
            refresh();
        });
        swMeteor.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setMeteor(this, c);
            if (c) NyanConfig.setSnow(this, false);
            refresh();
        });
    }

    private void refresh() {
        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));
    }
}

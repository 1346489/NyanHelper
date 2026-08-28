package com.moe.nyanhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FeaturesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this, findViewById(android.R.id.content));
        setContentView(R.layout.activity_features);

        // 关键：用 View 接收 findViewById 结果（避免 import 问题）
        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.apply(this, rootView);
        }

        Switch swAddNya = findViewById(R.id.swAddNya);
        Switch swYouToMaster = findViewById(R.id.swYouToMaster);
        Switch swIToMe = findViewById(R.id.swIToMe);

        swAddNya.setChecked(NyanConfig.isAddNya(this));
        swYouToMaster.setChecked(NyanConfig.isYouToMaster(this));
        swIToMe.setChecked(NyanConfig.isIToMe(this));

        swAddNya.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setAddNya(this, isChecked);
            Toast.makeText(this, "句尾加喵：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });

        swYouToMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setYouToMaster(this, isChecked);
            Toast.makeText(this, "你→主人：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });

        swIToMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NyanConfig.setIToMe(this, isChecked);
            Toast.makeText(this, "我→本喵：" + (isChecked ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
    }
}

package com.moe.nyanhelper;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FeaturesActivity extends AppCompatActivity {

    private Switch swAddNya, swYouToMaster, swIToMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        ThemeManager.apply(this, findViewById(R.id.root));

        swAddNya = findViewById(R.id.swAddNya);
        swYouToMaster = findViewById(R.id.swYouToMaster);
        swIToMe = findViewById(R.id.swIToMe);

        swAddNya.setChecked(NyanConfig.isAddNya(this));
        swYouToMaster.setChecked(NyanConfig.isYouToMaster(this));
        swIToMe.setChecked(NyanConfig.isIToMe(this));

        swAddNya.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setAddNya(this, c);
            Toast.makeText(this, "句尾加喵：" + (c ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
        swYouToMaster.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setYouToMaster(this, c);
            Toast.makeText(this, "你→主人：" + (c ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
        swIToMe.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setIToMe(this, c);
            Toast.makeText(this, "我→本喵：" + (c ? "开" : "关"), Toast.LENGTH_SHORT).show();
        });
    }
}

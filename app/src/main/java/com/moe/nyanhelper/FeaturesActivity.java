package com.moe.nyanhelper;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FeaturesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        Switch sw1 = findViewById(R.id.swAddNya);
        Switch sw2 = findViewById(R.id.swReplaceYou);
        Switch sw3 = findViewById(R.id.swReplaceMe);

        sw1.setChecked(NyanConfig.isAddNya(this));
        sw2.setChecked(NyanConfig.isReplaceYou(this));
        sw3.setChecked(NyanConfig.isReplaceMe(this));

        sw1.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setAddNya(this, c);
            Toast.makeText(this, "已" + (c ? "开启" : "关闭") + "结尾加喵喵~", Toast.LENGTH_SHORT).show();
        });
        sw2.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setReplaceYou(this, c);
            Toast.makeText(this, c ? "「你」→「主人」已开启" : "已关闭替换", Toast.LENGTH_SHORT).show();
        });
        sw3.setOnCheckedChangeListener((b, c) -> {
            NyanConfig.setReplaceMe(this, c);
            Toast.makeText(this, c ? "「我」→「本喵」已开启" : "已关闭替换", Toast.LENGTH_SHORT).show();
        });
    }
}

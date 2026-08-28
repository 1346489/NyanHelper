package com.moe.nyanhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        View.OnClickListener pick = v -> {
            int t = (int) v.getTag();
            NyanConfig.setTheme(this, t);
            Toast.makeText(this, "主题已切换喵~", Toast.LENGTH_SHORT).show();
            applyPreview(t);
        };

        View pink = findViewById(R.id.themePink);
        View green = findViewById(R.id.themeGreen);
        View purple = findViewById(R.id.themePurple);
        pink.setTag(0); green.setTag(1); purple.setTag(2);
        pink.setOnClickListener(pick);
        green.setOnClickListener(pick);
        purple.setOnClickListener(pick);
    }

    private void applyPreview(int t) {
        int color = (t == 1) ? 0xFFE8F5E9 : (t == 2) ? 0xFF2A2348 : 0xFFFFF0F6;
        findViewById(R.id.previewBg).setBackgroundColor(color);
    }
}

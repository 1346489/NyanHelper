package com.moe.nyanhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ThemeActivity extends AppCompatActivity {

    private Button btnSakura, btnMint, btnStarry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        ThemeManager.apply(this, findViewById(R.id.root));

        btnSakura = findViewById(R.id.btnSakura);
        btnMint = findViewById(R.id.btnMint);
        btnStarry = findViewById(R.id.btnStarry);
        updateButtons();

        btnSakura.setOnClickListener(v -> { setTheme(NyanConfig.THEME_SAKURA, "樱花粉"); });
        btnMint.setOnClickListener(v -> { setTheme(NyanConfig.THEME_MINT, "薄荷绿"); });
        btnStarry.setOnClickListener(v -> { setTheme(NyanConfig.THEME_STARRY, "星空紫"); });
    }

    private void setTheme(int theme, String name) {
        NyanConfig.setTheme(this, theme);
        ThemeManager.apply(this, findViewById(R.id.root));
        updateButtons();
        Toast.makeText(this, "已切换：" + name, Toast.LENGTH_SHORT).show();
    }

    private void updateButtons() {
        int t = NyanConfig.getTheme(this);
        btnSakura.setAlpha(t == NyanConfig.THEME_SAKURA ? 1.0f : 0.5f);
        btnMint.setAlpha(t == NyanConfig.THEME_MINT ? 1.0f : 0.5f);
        btnStarry.setAlpha(t == NyanConfig.THEME_STARRY ? 1.0f : 0.5f);
    }
}

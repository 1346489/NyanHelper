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
        ThemeManager.apply(this, findViewById(android.R.id.content));
        setContentView(R.layout.activity_theme);

        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.apply(this, rootView);
        }

        btnSakura = findViewById(R.id.btnSakura);
        btnMint = findViewById(R.id.btnMint);
        btnStarry = findViewById(R.id.btnStarry);

        updateButtons();

        btnSakura.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_SAKURA);
            Toast.makeText(this, "樱粉主题", Toast.LENGTH_SHORT).show();
            updateButtons();
            refreshTheme();
        });

        btnMint.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_MINT);
            Toast.makeText(this, "薄荷主题", Toast.LENGTH_SHORT).show();
            updateButtons();
            refreshTheme();
        });

        btnStarry.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_STARRY);
            Toast.makeText(this, "星空主题", Toast.LENGTH_SHORT).show();
            updateButtons();
            refreshTheme();
        });
    }

    private void updateButtons() {
        int theme = NyanConfig.getTheme(this);
        btnSakura.setAlpha(theme == NyanConfig.THEME_SAKURA ? 1.0f : 0.5f);
        btnMint.setAlpha(theme == NyanConfig.THEME_MINT ? 1.0f : 0.5f);
        btnStarry.setAlpha(theme == NyanConfig.THEME_STARRY ? 1.0f : 0.5f);
    }

    /** 切换后让当前页面背景也立即变化 */
    private void refreshTheme() {
        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.apply(this, rootView);
        }
    }
}

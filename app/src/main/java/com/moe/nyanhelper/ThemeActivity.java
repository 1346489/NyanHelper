package com.moe.nyanhelper;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        View rootView = findViewById(R.id.root);
        if (rootView != null) {
            ThemeManager.applyTheme(this, rootView);
        }

        TextView btnSakura = findViewById(R.id.btnSakura);
        TextView btnMint = findViewById(R.id.btnMint);
        TextView btnStarry = findViewById(R.id.btnStarry);

        updateSelection(btnSakura, btnMint, btnStarry);

        btnSakura.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_SAKURA);
            updateSelection(btnSakura, btnMint, btnStarry);
            ThemeManager.applyTheme(this, rootView);
            Toast.makeText(this, "已切换：樱花粉", Toast.LENGTH_SHORT).show();
        });

        btnMint.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_MINT);
            updateSelection(btnSakura, btnMint, btnStarry);
            ThemeManager.applyTheme(this, rootView);
            Toast.makeText(this, "已切换：薄荷绿", Toast.LENGTH_SHORT).show();
        });

        btnStarry.setOnClickListener(v -> {
            NyanConfig.setTheme(this, NyanConfig.THEME_STARRY);
            updateSelection(btnSakura, btnMint, btnStarry);
            ThemeManager.applyTheme(this, rootView);
            Toast.makeText(this, "已切换：星空紫", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSelection(TextView btnSakura, TextView btnMint, TextView btnStarry) {
        int theme = NyanConfig.getTheme(this);
        btnSakura.setAlpha(theme == NyanConfig.THEME_SAKURA ? 1.0f : 0.5f);
        btnMint.setAlpha(theme == NyanConfig.THEME_MINT ? 1.0f : 0.5f);
        btnStarry.setAlpha(theme == NyanConfig.THEME_STARRY ? 1.0f : 0.5f);
    }
}

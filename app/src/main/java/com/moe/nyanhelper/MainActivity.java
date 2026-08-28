package com.moe.nyanhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvFloat, tvAccess, tvService;
    private Button btnToggle, btnAccess, btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView avatar = findViewById(R.id.avatar);
        tvFloat = findViewById(R.id.tvFloatStatus);
        tvAccess = findViewById(R.id.tvAccessStatus);
        tvService = findViewById(R.id.tvServiceStatus);
        btnToggle = findViewById(R.id.btnFloatToggle);
        btnAccess = findViewById(R.id.btnAccessOpen);
        btnRefresh = findViewById(R.id.btnRefresh);

        avatar.setImageResource(R.drawable.nyan_avatar);

        btnToggle.setOnClickListener(v -> toggle());
        btnAccess.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnRefresh.setOnClickListener(v -> update());
        update();
    }

    private void update() {
        boolean hasFloat = Settings.canDrawOverlays(this);
        tvFloat.setText(hasFloat ? R.string.status_float_on : R.string.status_float_off);

        boolean hasAccess = false;
        for (android.accessibilityservice.AccessibilityServiceInfo info :
                ((android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))
                        .getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getId() != null && info.getId().contains("nyanhelper")) hasAccess = true;
        }
        tvAccess.setText(hasAccess ? R.string.status_access_on : R.string.status_access_off);

        boolean running = getSharedPreferences("nyan_config", MODE_PRIVATE).getBoolean("float_started", false);
        tvService.setText(running && hasFloat ? R.string.status_service_on : R.string.status_service_off);
        btnToggle.setText(running && hasFloat ? R.string.btn_float_off : R.string.btn_float_on);
    }

    private void toggle() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, R.string.toast_grant_overlay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, FloatWindowService.class);
        boolean running = getSharedPreferences("nyan_config", MODE_PRIVATE).getBoolean("float_started", false);
        if (running) {
            stopService(intent);
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit().putBoolean("float_started", false).apply();
            Toast.makeText(this, R.string.toast_float_stopped, Toast.LENGTH_SHORT).show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            getSharedPreferences("nyan_config", MODE_PRIVATE).edit().putBoolean("float_started", true).apply();
            Toast.makeText(this, R.string.toast_float_started, Toast.LENGTH_SHORT).show();
        }
        update();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }
}

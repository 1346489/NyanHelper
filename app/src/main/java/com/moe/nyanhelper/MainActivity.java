package com.benmao.assistant;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Bottom navigation
    private LinearLayout navHome, navPermission, navSettings;
    private ImageView iconHome, iconPermission, iconSettings;
    private TextView textHome, textPermission, textSettings;

    // Screens
    private View screenHome, screenPermission, screenSettings;

    // Settings
    private Switch switchDarkMode;

    // Permission status
    private TextView tvAccessibilityStatus, tvOverlayStatus;
    private View btnAccessibility, btnOverlay;

    // App state
    public static boolean darkModeEnabled = false;
    public static boolean accessibilityEnabled = false;
    public static boolean overlayEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBottomNav();
        initSettings();
        initPermissionPage();
        checkPermissions();

        // Default to home screen
        showScreen(0);
    }

    private void initViews() {
        // Bottom nav views
        navHome = findViewById(R.id.nav_home);
        navPermission = findViewById(R.id.nav_permission);
        navSettings = findViewById(R.id.nav_settings);
        iconHome = findViewById(R.id.icon_home);
        iconPermission = findViewById(R.id.icon_permission);
        iconSettings = findViewById(R.id.icon_settings);
        textHome = findViewById(R.id.text_home);
        textPermission = findViewById(R.id.text_permission);
        textSettings = findViewById(R.id.text_settings);

        // Screens
        screenHome = findViewById(R.id.screen_home);
        screenPermission = findViewById(R.id.screen_permission);
        screenSettings = findViewById(R.id.screen_settings);
    }

    private void initBottomNav() {
        navHome.setOnClickListener(v -> showScreen(0));
        navPermission.setOnClickListener(v -> showScreen(1));
        navSettings.setOnClickListener(v -> showScreen(2));
    }

    private void showScreen(int index) {
        // Reset all nav items
        iconHome.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        iconPermission.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        iconSettings.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        textHome.setTextColor(ContextCompat.getColor(this, R.color.gray));
        textPermission.setTextColor(ContextCompat.getColor(this, R.color.gray));
        textSettings.setTextColor(ContextCompat.getColor(this, R.color.gray));

        screenHome.setVisibility(View.GONE);
        screenPermission.setVisibility(View.GONE);
        screenSettings.setVisibility(View.GONE);

        // Highlight selected
        int pink = ContextCompat.getColor(this, R.color.pink_accent);
        switch (index) {
            case 0:
                screenHome.setVisibility(View.VISIBLE);
                iconHome.setColorFilter(pink);
                textHome.setTextColor(pink);
                break;
            case 1:
                screenPermission.setVisibility(View.VISIBLE);
                iconPermission.setColorFilter(pink);
                textPermission.setTextColor(pink);
                break;
            case 2:
                screenSettings.setVisibility(View.VISIBLE);
                iconSettings.setColorFilter(pink);
                textSettings.setTextColor(pink);
                break;
        }
    }

    // ===================== SETTINGS =====================

    private void initSettings() {
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            darkModeEnabled = isChecked;
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(this, isChecked ? "深色模式已开启" : "深色模式已关闭", Toast.LENGTH_SHORT).show();
        });

        // Version text
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version));

        // Launch overlay button
        View btnLaunch = findViewById(R.id.btn_launch_overlay);
        btnLaunch.setOnClickListener(v -> launchOverlay());
    }

    private void launchOverlay() {
        if (!checkOverlayPermission()) {
            Toast.makeText(this, R.string.please_enable_overlay, Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
            return;
        }
        if (!checkAccessibilityPermission()) {
            Toast.makeText(this, R.string.please_enable_accessibility, Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the overlay service / show floating window
        Intent intent = new Intent(this, OverlayWindowService.class);
        intent.setAction(OverlayWindowService.ACTION_SHOW);
        startService(intent);
        Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show();
    }

    // ===================== PERMISSIONS =====================

    private void initPermissionPage() {
        tvAccessibilityStatus = findViewById(R.id.tv_accessibility_status);
        tvOverlayStatus = findViewById(R.id.tv_overlay_status);
        btnAccessibility = findViewById(R.id.btn_accessibility);
        btnOverlay = findViewById(R.id.btn_overlay);

        btnAccessibility.setOnClickListener(v -> {
            // Open accessibility settings
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "请找到「本喵助手」并开启无障碍服务", Toast.LENGTH_LONG).show();
        });

        btnOverlay.setOnClickListener(v -> {
            requestOverlayPermission();
        });
    }

    private void checkPermissions() {
        accessibilityEnabled = checkAccessibilityPermission();
        overlayEnabled = checkOverlayPermission();

        updatePermissionUI();
    }

    private void updatePermissionUI() {
        if (accessibilityEnabled) {
            tvAccessibilityStatus.setText(R.string.enabled);
            tvAccessibilityStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            tvAccessibilityStatus.setText(R.string.disabled);
            tvAccessibilityStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

        if (overlayEnabled) {
            tvOverlayStatus.setText(R.string.enabled);
            tvOverlayStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            tvOverlayStatus.setText(R.string.disabled);
            tvOverlayStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
        }
    }

    private boolean checkAccessibilityPermission() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabledServices) {
            if (info.getId().contains("com.benmao.assistant")) {
                return true;
            }
        }
        // Also check our service specifically
        try {
            String enabledServicesSetting = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServicesSetting != null && enabledServicesSetting.contains("com.benmao.assistant")) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean checkOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "请开启「本喵助手」的悬浮窗权限", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }
}

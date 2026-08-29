package com.benmao.assistant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import java.util.Random;

public class OverlayWindowService extends Service {

    public static final String ACTION_SHOW = "com.benmao.assistant.ACTION_SHOW";
    public static final String ACTION_HIDE = "com.benmao.assistant.ACTION_HIDE";

    private WindowManager windowManager;
    private SharedPreferences prefs;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private View floatingBall;
    private View overlayWindow;
    private ViewGroup contentContainer;

    // Layout params
    private WindowManager.LayoutParams ballParams;
    private WindowManager.LayoutParams windowParams;

    // State
    private boolean isWindowVisible = false;
    private boolean isBallVisible = false;

    // Settings state
    private boolean funcAddMiao = false;
    private boolean funcReplaceMe = false;
    private boolean funcReplaceYou = false;
    private boolean settingSnow = false;
    private boolean settingMeteor = false;
    private boolean controlVolumeHide = true;

    // Theme
    private int bgColor = Color.WHITE;
    private int textColor = Color.BLACK;
    public static final int[] BG_COLORS = {Color.WHITE, 0xFFFFE4EC, 0xFF2D2D30};
    public static final int[] TEXT_COLORS = {Color.BLACK, Color.WHITE, 0xFF8B008B};

    // Snow/Meteor effect
    private ViewGroup effectContainer;
    private Runnable effectRunnable;
    private Handler effectHandler = new Handler();
    private Random random = new Random();

    // Volume key monitoring
    private VolumeKeyMonitor volumeMonitor;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        prefs = getSharedPreferences("benmao_prefs", Context.MODE_PRIVATE);
        loadSettings();

        createFloatingBall();
        createOverlayWindow();
        startSnowMeteorEffect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SHOW.equals(action)) {
                showFloatingBall();
            } else if (ACTION_HIDE.equals(action)) {
                hideAll();
            }
        }
        return START_STICKY;
    }

    // ===================== FLOATING BALL =====================

    private BallTouchListener ballTouchListener;

    private void createFloatingBall() {
        floatingBall = LayoutInflater.from(this).inflate(R.layout.floating_ball, null);

        ballParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        ballParams.gravity = Gravity.TOP | Gravity.START;
        ballParams.x = 100;
        ballParams.y = 300;

        // Touch to move and click
        ballTouchListener = new BallTouchListener(windowManager, ballParams);
        floatingBall.setOnTouchListener(ballTouchListener);
        floatingBall.setOnClickListener(v -> openMenuWithAnimation());
    }

    private void showFloatingBall() {
        if (!isBallVisible) {
            try {
                windowManager.addView(floatingBall, ballParams);
                isBallVisible = true;
                // Entrance animation
                playBallAppearAnimation();
            } catch (Exception e) {
                // View already added or permission denied
            }
        }
    }

    private void hideFloatingBall() {
        if (isBallVisible && floatingBall != null) {
            try {
                windowManager.removeView(floatingBall);
            } catch (Exception ignored) {}
            isBallVisible = false;
        }
    }

    private void playBallAppearAnimation() {
        ScaleAnimation scale = new ScaleAnimation(0f, 1f, 0f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(300);
        scale.setInterpolator(new OvershootInterpolator(1.2f));
        floatingBall.startAnimation(scale);
    }

    // ===================== OVERLAY WINDOW (Menu) =====================

    private void createOverlayWindow() {
        overlayWindow = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null);
        contentContainer = overlayWindow.findViewById(R.id.content_container);
        effectContainer = overlayWindow.findViewById(R.id.effect_container);

        windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        windowParams.gravity = Gravity.CENTER;

        setupMenuListeners();
        setupAnnouncementPage();
        setupFunctionsPage();
        setupSettingsPage();
        setupControlPage();
        setupThemePage();
    }

    private void setupMenuListeners() {
        // Menu items: 公告, 功能, 设置, 控制, 主题
        View menuAnnouncement = overlayWindow.findViewById(R.id.menu_announcement);
        View menuFunctions = overlayWindow.findViewById(R.id.menu_functions);
        View menuSettings = overlayWindow.findViewById(R.id.menu_settings);
        View menuControl = overlayWindow.findViewById(R.id.menu_control);
        View menuTheme = overlayWindow.findViewById(R.id.menu_theme);
        View btnClose = overlayWindow.findViewById(R.id.btn_close);

        menuAnnouncement.setOnClickListener(v -> showPage(R.id.page_announcement));
        menuFunctions.setOnClickListener(v -> showPage(R.id.page_functions));
        menuSettings.setOnClickListener(v -> showPage(R.id.page_settings));
        menuControl.setOnClickListener(v -> showPage(R.id.page_control));
        menuTheme.setOnClickListener(v -> showPage(R.id.page_theme));

        btnClose.setOnClickListener(v -> closeMenuToBall());

        // Update selection state
        setMenuSelected(menuAnnouncement, true);
    }

    private void setMenuSelected(View menuItem, boolean selected) {
        // Reset all
        int normalBg = ContextCompat.getColor(this, R.color.overlay_card);
        int selectedBg = ContextCompat.getColor(this, R.color.pink_primary);

        View[] menus = {
                overlayWindow.findViewById(R.id.menu_announcement),
                overlayWindow.findViewById(R.id.menu_functions),
                overlayWindow.findViewById(R.id.menu_settings),
                overlayWindow.findViewById(R.id.menu_control),
                overlayWindow.findViewById(R.id.menu_theme)
        };
        for (View m : menus) {
            m.setBackgroundColor(normalBg);
        }
        if (selected && menuItem != null) {
            menuItem.setBackgroundColor(selectedBg);
        }
    }

    private void showPage(int pageId) {
        // Hide all pages
        overlayWindow.findViewById(R.id.page_announcement).setVisibility(View.GONE);
        overlayWindow.findViewById(R.id.page_functions).setVisibility(View.GONE);
        overlayWindow.findViewById(R.id.page_settings).setVisibility(View.GONE);
        overlayWindow.findViewById(R.id.page_control).setVisibility(View.GONE);
        overlayWindow.findViewById(R.id.page_theme).setVisibility(View.GONE);

        // Show selected with animation
        View page = overlayWindow.findViewById(pageId);
        page.setVisibility(View.VISIBLE);
        playPageEnterAnimation(page);

        // Update menu selection
        if (pageId == R.id.page_announcement)
            setMenuSelected(overlayWindow.findViewById(R.id.menu_announcement), true);
        else if (pageId == R.id.page_functions)
            setMenuSelected(overlayWindow.findViewById(R.id.menu_functions), true);
        else if (pageId == R.id.page_settings)
            setMenuSelected(overlayWindow.findViewById(R.id.menu_settings), true);
        else if (pageId == R.id.page_control)
            setMenuSelected(overlayWindow.findViewById(R.id.menu_control), true);
        else if (pageId == R.id.page_theme)
            setMenuSelected(overlayWindow.findViewById(R.id.menu_theme), true);
    }

    private void playPageEnterAnimation(View view) {
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setDuration(200);
        ScaleAnimation scale = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(200);
        view.startAnimation(alpha);
        view.startAnimation(scale);
    }

    // ===================== OPEN / CLOSE ANIMATIONS =====================

    private void openMenuWithAnimation() {
        if (isWindowVisible) return;

        hideFloatingBall();

        try {
            windowManager.addView(overlayWindow, windowParams);
            isWindowVisible = true;

            // Play open animation: scale up from ball position
            overlayWindow.setAlpha(0f);
            overlayWindow.setScaleX(0.3f);
            overlayWindow.setScaleY(0.3f);

            overlayWindow.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(350)
                    .setInterpolator(new OvershootInterpolator(0.8f))
                    .start();

            // Start volume monitoring
            if (controlVolumeHide) {
                startVolumeMonitoring();
            }

        } catch (Exception e) {
            // Re-show ball if window fails
            showFloatingBall();
        }
    }

    private void closeMenuToBall() {
        if (!isWindowVisible) return;

        // Animate: scale down to ball
        overlayWindow.animate()
                .alpha(0f)
                .scaleX(0.3f)
                .scaleY(0.3f)
                .setDuration(250)
                .withEndAction(() -> {
                    if (overlayWindow != null && overlayWindow.getParent() != null) {
                        try {
                            windowManager.removeView(overlayWindow);
                        } catch (Exception ignored) {}
                    }
                    isWindowVisible = false;
                    showFloatingBall();
                })
                .start();

        stopVolumeMonitoring();
    }

    private void hideAll() {
        hideFloatingBall();
        if (isWindowVisible && overlayWindow != null) {
            try {
                windowManager.removeView(overlayWindow);
            } catch (Exception ignored) {}
            isWindowVisible = false;
        }
        stopVolumeMonitoring();
    }

    // ===================== PAGE: ANNOUNCEMENT =====================

    private void setupAnnouncementPage() {
        TextView announcement1 = overlayWindow.findViewById(R.id.announcement_1);
        TextView announcement2 = overlayWindow.findViewById(R.id.announcement_2);
        announcement1.setText(R.string.announcement1);
        announcement2.setText(R.string.announcement2);
    }

    // ===================== PAGE: FUNCTIONS =====================

    private void setupFunctionsPage() {
        androidx.appcompat.widget.SwitchCompat switchMiao = overlayWindow.findViewById(R.id.switch_add_miao);
        androidx.appcompat.widget.SwitchCompat switchReplaceMe = overlayWindow.findViewById(R.id.switch_replace_me);
        androidx.appcompat.widget.SwitchCompat switchReplaceYou = overlayWindow.findViewById(R.id.switch_replace_you);

        switchMiao.setChecked(funcAddMiao);
        switchReplaceMe.setChecked(funcReplaceMe);
        switchReplaceYou.setChecked(funcReplaceYou);

        switchMiao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            funcAddMiao = isChecked;
            saveSettings();
            notifyAccessibilityService();
        });

        switchReplaceMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            funcReplaceMe = isChecked;
            saveSettings();
            notifyAccessibilityService();
        });

        switchReplaceYou.setOnCheckedChangeListener((buttonView, isChecked) -> {
            funcReplaceYou = isChecked;
            saveSettings();
            notifyAccessibilityService();
        });
    }

    private void notifyAccessibilityService() {
        // Send broadcast to accessibility service to update settings
        Intent intent = new Intent(BenmaoAccessibilityService.ACTION_UPDATE_SETTINGS);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    // ===================== PAGE: SETTINGS (Snow/Meteor) =====================

    private void setupSettingsPage() {
        androidx.appcompat.widget.SwitchCompat switchSnow = overlayWindow.findViewById(R.id.switch_snow);
        androidx.appcompat.widget.SwitchCompat switchMeteor = overlayWindow.findViewById(R.id.switch_meteor);

        switchSnow.setChecked(settingSnow);
        switchMeteor.setChecked(settingMeteor);

        switchSnow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                settingMeteor = false;
                androidx.appcompat.widget.SwitchCompat meteorSwitch = overlayWindow.findViewById(R.id.switch_meteor);
                if (meteorSwitch != null) meteorSwitch.setChecked(false);
            }
            settingSnow = isChecked;
            saveSettings();
            updateEffect();
        });

        switchMeteor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                settingSnow = false;
                androidx.appcompat.widget.SwitchCompat snowSwitch = overlayWindow.findViewById(R.id.switch_snow);
                if (snowSwitch != null) snowSwitch.setChecked(false);
            }
            settingMeteor = isChecked;
            saveSettings();
            updateEffect();
        });
    }

    // ===================== PAGE: CONTROL =====================

    private void setupControlPage() {
        androidx.appcompat.widget.SwitchCompat switchVolumeHide = overlayWindow.findViewById(R.id.switch_volume_hide);
        switchVolumeHide.setChecked(controlVolumeHide);

        switchVolumeHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            controlVolumeHide = isChecked;
            saveSettings();
            if (isChecked && isWindowVisible) {
                startVolumeMonitoring();
            } else {
                stopVolumeMonitoring();
            }
        });
    }

    // ===================== PAGE: THEME =====================

    private void setupThemePage() {
        // Background color options (3)
        View bgOption1 = overlayWindow.findViewById(R.id.bg_color_1);
        View bgOption2 = overlayWindow.findViewById(R.id.bg_color_2);
        View bgOption3 = overlayWindow.findViewById(R.id.bg_color_3);

        bgOption1.setOnClickListener(v -> { bgColor = BG_COLORS[0]; applyTheme(); saveSettings(); });
        bgOption2.setOnClickListener(v -> { bgColor = BG_COLORS[1]; applyTheme(); saveSettings(); });
        bgOption3.setOnClickListener(v -> { bgColor = BG_COLORS[2]; applyTheme(); saveSettings(); });

        // Text color options (3 different)
        View textOption1 = overlayWindow.findViewById(R.id.text_color_1);
        View textOption2 = overlayWindow.findViewById(R.id.text_color_2);
        View textOption3 = overlayWindow.findViewById(R.id.text_color_3);

        textOption1.setOnClickListener(v -> { textColor = TEXT_COLORS[0]; applyTheme(); saveSettings(); });
        textOption2.setOnClickListener(v -> { textColor = TEXT_COLORS[1]; applyTheme(); saveSettings(); });
        textOption3.setOnClickListener(v -> { textColor = TEXT_COLORS[2]; applyTheme(); saveSettings(); });

        // Reset button
        overlayWindow.findViewById(R.id.btn_reset_theme).setOnClickListener(v -> {
            bgColor = Color.WHITE;
            textColor = Color.BLACK;
            applyTheme();
            saveSettings();
            Toast.makeText(this, "已恢复默认主题", Toast.LENGTH_SHORT).show();
        });

        applyTheme();
    }

    private void applyTheme() {
        if (contentContainer != null) {
            contentContainer.setBackgroundColor(bgColor);
        }
        // Update text colors recursively
        updateTextColors(contentContainer, textColor);
    }

    private void updateTextColors(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                updateTextColors(group.getChildAt(i), color);
            }
        }
    }

    // ===================== SNOW / METEOR EFFECT =====================

    private void startSnowMeteorEffect() {
        updateEffect();
    }

    private void updateEffect() {
        if (effectRunnable != null) {
            effectHandler.removeCallbacks(effectRunnable);
        }

        if (!settingSnow && !settingMeteor) return;

        effectRunnable = new Runnable() {
            @Override
            public void run() {
                if (isWindowVisible && effectContainer != null) {
                    spawnEffectParticle();
                }
                effectHandler.postDelayed(this, settingSnow ? 200 : 100);
            }
        };
        effectHandler.post(effectRunnable);
    }

    private void spawnEffectParticle() {
        if (effectContainer == null) return;

        View particle = new View(this);
        int size = settingSnow ? 8 + random.nextInt(12) : 4 + random.nextInt(6);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);

        int startX = random.nextInt(Math.max(1, effectContainer.getWidth()));
        params.leftMargin = startX;
        params.topMargin = -size;

        particle.setLayoutParams(params);

        if (settingSnow) {
            particle.setBackgroundResource(R.drawable.bg_snow_particle);
        } else {
            particle.setBackgroundResource(R.drawable.bg_meteor_particle);
        }

        effectContainer.addView(particle);

        // Animate falling
        int duration = settingSnow ? 3000 + random.nextInt(2000) : 1500 + random.nextInt(1000);
        int endY = effectContainer.getHeight() + size;

        particle.animate()
                .translationY(endY)
                .alpha(settingSnow ? 0.6f : 1.0f)
                .setDuration(duration)
                .withEndAction(() -> {
                    if (particle.getParent() != null) {
                        ((ViewGroup) particle.getParent()).removeView(particle);
                    }
                })
                .start();

        // Limit particles
        if (effectContainer.getChildCount() > 50) {
            View first = effectContainer.getChildAt(0);
            if (first != null && first.getParent() != null) {
                ((ViewGroup) first.getParent()).removeView(first);
            }
        }
    }

    // ===================== VOLUME KEY MONITORING =====================

    private void startVolumeMonitoring() {
        if (volumeMonitor == null) {
            volumeMonitor = new VolumeKeyMonitor(this);
        }
        volumeMonitor.start(new VolumeKeyMonitor.VolumeKeyListener() {
            @Override
            public void onVolumeUp() {
                // Volume up: restore menu from ball
                mainHandler.post(() -> {
                    if (!isWindowVisible && isBallVisible) {
                        openMenuWithAnimation();
                    }
                });
            }

            @Override
            public void onVolumeDown() {
                // Volume down: close menu back to ball
                mainHandler.post(() -> {
                    if (isWindowVisible) {
                        closeMenuToBall();
                    }
                });
            }
        });
    }

    private void stopVolumeMonitoring() {
        if (volumeMonitor != null) {
            volumeMonitor.stop();
        }
    }

    // ===================== PREFERENCES =====================

    private void loadSettings() {
        funcAddMiao = prefs.getBoolean("func_add_miao", false);
        funcReplaceMe = prefs.getBoolean("func_replace_me", false);
        funcReplaceYou = prefs.getBoolean("func_replace_you", false);
        settingSnow = prefs.getBoolean("setting_snow", false);
        settingMeteor = prefs.getBoolean("setting_meteor", false);
        controlVolumeHide = prefs.getBoolean("control_volume_hide", true);
        bgColor = prefs.getInt("bg_color", Color.WHITE);
        textColor = prefs.getInt("text_color", Color.BLACK);
    }

    private void saveSettings() {
        prefs.edit()
                .putBoolean("func_add_miao", funcAddMiao)
                .putBoolean("func_replace_me", funcReplaceMe)
                .putBoolean("func_replace_you", funcReplaceYou)
                .putBoolean("setting_snow", settingSnow)
                .putBoolean("setting_meteor", settingMeteor)
                .putBoolean("control_volume_hide", controlVolumeHide)
                .putInt("bg_color", bgColor)
                .putInt("text_color", textColor)
                .apply();
    }

    // ===================== UTILITIES =====================

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideAll();
        stopVolumeMonitoring();
        if (effectHandler != null && effectRunnable != null) {
            effectHandler.removeCallbacks(effectRunnable);
        }
    }
}

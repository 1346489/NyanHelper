package com.benmao.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.benmao.assistant.databinding.WindowMenuBinding;

public class OverlayWindowService extends Service {

    public static final String ACTION_VOLUME_UP = "com.benmao.assistant.VOLUME_UP";
    public static final String ACTION_VOLUME_DOWN = "com.benmao.assistant.VOLUME_DOWN";

    private WindowManager wm;
    private View ballView;
    private View menuView;
    private WindowManager.LayoutParams ballParams;
    private WindowManager.LayoutParams menuParams;
    private boolean menuShown = false;
    private float downX, downY;
    private int initialX, initialY;
    private boolean isMoving = false;

    private int overlayType;
    private Prefs prefs;
    private WindowMenuBinding m;

    // 主题
    public static int bgIndex = 0;   // 0白 1粉 2深色
    public static int textIndex = 0; // 0黑 1白 2紫

    private final int[] bgColors = {0xFFFFFFFF, 0xFFFFD6E7, 0xFF1E1E2C};
    private final int[] textColors = {0xFF000000, 0xFFFFFFFF, 0xFF9C27B0};

    private BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_VOLUME_UP.equals(action)) {
                showMenu();
            } else if (ACTION_VOLUME_DOWN.equals(action)) {
                hideMenu();
                showBall();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(this);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 悬浮窗类型兼容：API26+ 用 TYPE_APPLICATION_OVERLAY，旧版用 TYPE_PHONE
        overlayType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        startForegroundCompat();

        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_VOLUME_UP);
        f.addAction(ACTION_VOLUME_DOWN);
        registerReceiver(volumeReceiver, f);

        createBall();
    }

    private void startForegroundCompat() {
        String channelId = "benmao_overlay";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "本喵助手前台服务", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }
    }

    private void createBall() {
        ballView = LayoutInflater.from(this).inflate(R.layout.floating_ball, null);
        ballParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        ballParams.gravity = Gravity.TOP | Gravity.START;
        ballParams.x = 100;
        ballParams.y = 300;

        ballView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    initialX = ballParams.x;
                    initialY = ballParams.y;
                    isMoving = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) isMoving = true;
                    ballParams.x = initialX + (int) dx;
                    ballParams.y = initialY + (int) dy;
                    wm.updateViewLayout(ballView, ballParams);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!isMoving) {
                        // 点击 -> 打开菜单（带动画）
                        openMenuWithAnimation();
                    }
                    return true;
            }
            return false;
        });

        try {
            wm.addView(ballView, ballParams);
        } catch (Exception e) {
            Toast.makeText(this, "悬浮窗显示失败，请在系统设置允许「显示悬浮窗」", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void openMenuWithAnimation() {
        if (menuShown) return;
        hideBall();
        showMenu();
        // 打开动画：从悬浮球位置缩放展开
        if (menuView != null) {
            ScaleAnimation sa = new ScaleAnimation(0.2f, 1f, 0.2f, 1f,
                    menuView.getWidth() / 2f, menuView.getHeight() / 2f);
            sa.setDuration(300);
            sa.setInterpolator(new OvershootInterpolator());
            menuView.startAnimation(sa);
        }
    }

    private void showMenu() {
        if (menuShown) return;
        if (menuView == null) buildMenu();
        menuView.setVisibility(View.VISIBLE);
        menuShown = true;
    }

    private void hideMenu() {
        if (!menuShown || menuView == null) return;
        menuView.setVisibility(View.GONE);
        menuShown = false;
    }

    private void showBall() {
        if (ballView != null) ballView.setVisibility(View.VISIBLE);
    }

    private void hideBall() {
        if (ballView != null) ballView.setVisibility(View.GONE);
    }

    private void buildMenu() {
        m = WindowMenuBinding.inflate(LayoutInflater.from(this));
        menuView = m.getRoot();
        menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                0,
                PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.CENTER;
        try {
            wm.addView(m.getRoot(), menuParams);
        } catch (Exception e) {
            Toast.makeText(this, "菜单显示失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            menuView = null;
            return;
        }
        menuShown = true;

        applyTheme();
        setupMenu();
    }

    private void applyTheme() {
        if (m == null) return;
        int bg = bgColors[bgIndex];
        int tc = textColors[textIndex];
        if (m.menuRoot != null) m.menuRoot.setBackgroundColor(bg);
        // 递归设置文字颜色
        if (m.menuRoot != null) setTextColor(m.menuRoot, tc);
    }

    // 统一异常兜底：悬浮窗内任何操作出错都只提示、不崩溃
    private void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            try {
                Toast.makeText(this, "操作出错：" + t.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        }
    }

    private void setTextColor(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        } else if (view instanceof SwitchCompat) {
            // Switch 保持原样
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                setTextColor(vg.getChildAt(i), color);
            }
        }
    }

    private void setupMenu() {
        if (m == null) return;
        // 左侧菜单项点击
        safeRun(() -> m.navAnnounce.setOnClickListener(v -> safeRun(() -> showPage(0))));
        safeRun(() -> m.navFunction.setOnClickListener(v -> safeRun(() -> showPage(1))));
        safeRun(() -> m.navSettings2.setOnClickListener(v -> safeRun(() -> showPage(2))));
        safeRun(() -> m.navControl.setOnClickListener(v -> safeRun(() -> showPage(3))));
        safeRun(() -> m.navTheme.setOnClickListener(v -> safeRun(() -> showPage(4))));

        // 关闭（X）-> 变回悬浮球
        safeRun(() -> m.btnClose.setOnClickListener(v -> safeRun(() -> {
            hideMenu();
            showBall();
        })));

        // ---- 功能页：三个开关（无障碍实现） ----
        safeRun(() -> m.switchAddMeow.setOnCheckedChangeListener((b, c) -> safeRun(() -> prefs.setAddMeow(c))));
        safeRun(() -> m.switchReplaceMe.setOnCheckedChangeListener((b, c) -> safeRun(() -> prefs.setReplaceMe(c))));
        safeRun(() -> m.switchReplaceYou.setOnCheckedChangeListener((b, c) -> safeRun(() -> prefs.setReplaceYou(c))));
        safeRun(() -> m.switchAddMeow.setChecked(prefs.isAddMeow()));
        safeRun(() -> m.switchReplaceMe.setChecked(prefs.isReplaceMe()));
        safeRun(() -> m.switchReplaceYou.setChecked(prefs.isReplaceYou()));

        // ---- 设置页：雪花 / 流星雨（互斥） ----
        safeRun(() -> m.switchSnow.setOnCheckedChangeListener((b, c) -> safeRun(() -> {
            if (c) {
                prefs.setSnow(true);
                prefs.setMeteor(false);
                if (m.switchMeteor != null) m.switchMeteor.setChecked(false);
            } else {
                prefs.setSnow(false);
            }
            updateParticle();
        })));

        safeRun(() -> m.switchMeteor.setOnCheckedChangeListener((b, c) -> safeRun(() -> {
            if (c) {
                prefs.setMeteor(true);
                prefs.setSnow(false);
                if (m.switchSnow != null) m.switchSnow.setChecked(false);
            } else {
                prefs.setMeteor(false);
            }
            updateParticle();
        })));

        safeRun(() -> m.switchSnow.setChecked(prefs.isSnow()));
        safeRun(() -> m.switchMeteor.setChecked(prefs.isMeteor()));

        // ---- 控制页：音量键隐藏（默认开） ----
        safeRun(() -> m.switchVolumeHide.setChecked(prefs.isVolumeHide()));
        safeRun(() -> m.switchVolumeHide.setOnCheckedChangeListener((b, c) -> safeRun(() -> prefs.setVolumeHide(c))));

        // ---- 主题页 ----
        // 背景：白(0)/粉(1)/深色(2)；文字：黑(0)/白(1)/紫(2)
        final int[] bgViews = {R.id.color_bg_white, R.id.color_bg_pink, R.id.color_bg_dark};
        final int[] textViews = {R.id.color_text_black, R.id.color_text_white, R.id.color_text_purple};

        // 安全解析 tag：避免 (int)null 空指针崩溃
        View.OnClickListener bgClick = v -> safeRun(() -> {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                bgIndex = (Integer) tag;
                applyTheme();
                updateColorSelection(bgViews, bgIndex);
            }
        });
        safeRun(() -> {
            m.colorBgWhite.setTag(0); m.colorBgPink.setTag(1); m.colorBgDark.setTag(2);
            m.colorBgWhite.setOnClickListener(bgClick);
            m.colorBgPink.setOnClickListener(bgClick);
            m.colorBgDark.setOnClickListener(bgClick);
        });

        View.OnClickListener textClick = v -> safeRun(() -> {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                textIndex = (Integer) tag;
                applyTheme();
                updateColorSelection(textViews, textIndex);
            }
        });
        safeRun(() -> {
            m.colorTextBlack.setTag(0); m.colorTextWhite.setTag(1); m.colorTextPurple.setTag(2);
            m.colorTextBlack.setOnClickListener(textClick);
            m.colorTextWhite.setOnClickListener(textClick);
            m.colorTextPurple.setOnClickListener(textClick);
        });

        safeRun(() -> m.btnResetTheme.setOnClickListener(v -> safeRun(() -> {
            bgIndex = 0; textIndex = 0; // 白底黑字
            applyTheme();
            updateColorSelection(bgViews, bgIndex);
            updateColorSelection(textViews, textIndex);
        })));

        updateColorSelection(bgViews, bgIndex);
        updateColorSelection(textViews, textIndex);

        showPage(0);
    }

    private void showPage(int index) {
        if (m == null) return;
        // 每个页面单独判空，避免某个 id 缺失导致整页崩溃
        if (m.pageAnnounce != null) m.pageAnnounce.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        if (m.pageFunction != null) m.pageFunction.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        if (m.pageSettings2 != null) m.pageSettings2.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        if (m.pageControl != null) m.pageControl.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        if (m.pageTheme != null) m.pageTheme.setVisibility(index == 4 ? View.VISIBLE : View.GONE);
    }


    // 给选中的颜色圆点加白色描边，未选中的恢复默认
    private void updateColorSelection(int[] viewIds, int selectedIndex) {
        if (m == null) return;
        View root = m.getRoot();
        for (int i = 0; i < viewIds.length; i++) {
            View v = root.findViewById(viewIds[i]);
            if (v != null) {
                v.setAlpha(i == selectedIndex ? 1f : 0.45f);
            }
        }
    }

    private void updateParticle() {
        if (m == null || m.particleContainer == null) return;
        // m.particleContainer 就是 XML 里的 ParticleView，直接设模式，不用 addView
        safeRun(() -> m.particleContainer.setMode(prefs.isMeteor() ? 1 : (prefs.isSnow() ? 0 : -1)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ballView != null) wm.removeView(ballView);
        if (menuView != null) wm.removeView(menuView);
        try { unregisterReceiver(volumeReceiver); } catch (Exception ignored) {}
    }
}

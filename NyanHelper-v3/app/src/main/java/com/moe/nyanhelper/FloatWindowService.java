package com.moe.nyanhelper;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

/**
 * 悬浮窗服务：
 * - 圆形悬浮球（完美圆形，图片铺满）
 * - 展开面板含三页：功能 / 设置 / 主题
 * - 功能页：三个开关（结尾加喵 / 我->本喵 / 你->主人）
 * - 设置页：雪花 / 流星 两个互斥特效开关（动态绘制）
 * - 主题页：三套背景色选择
 */
public class FloatWindowService extends Service {

    private static final String CHANNEL = "nyan_channel";

    private WindowManager wm;
    private View root;
    private FrameLayout ball;
    private ImageView ballIcon;
    private FrameLayout panel;
    private ViewEffectOverlay effectOverlay;

    private WindowManager.LayoutParams params;

    // 三页
    private LinearLayout pageFunctions, pageSettings, pageTheme;
    private int currentPage = 0; // 0=功能 1=设置 2=主题

    private int screenW = 1080, screenH = 1920, statusBarH = 0, navBarH = 0;
    private boolean expanded = false, snapping = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotif());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        computeMetrics();

        root = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        ball = root.findViewById(R.id.float_ball);
        ballIcon = root.findViewById(R.id.float_ball_icon);
        panel = root.findViewById(R.id.float_panel);
        effectOverlay = root.findViewById(R.id.effect_overlay);

        // 圆形球：确保图片完美覆盖圆形裁剪
        ball.setClipToOutline(true);
        ballIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);

        // 页面容器
        pageFunctions = panel.findViewById(R.id.page_functions);
        pageSettings = panel.findViewById(R.id.page_settings);
        pageTheme = panel.findViewById(R.id.page_theme);
        setupTabs();
        setupFunctionPage();
        setupSettingPage();
        setupThemePage();
        showPage(0);

        panel.setVisibility(View.GONE);
        panel.setAlpha(0f);
        panel.setScaleX(0.3f);
        panel.setScaleY(0.3f);

        int size = dp(64);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = screenW - size - dp(24);
        params.y = dp(200);

        wm.addView(root, params);
        setupBallTouch();
        applyThemeColors();
    }

    /* ==================== 球拖动 ==================== */

    private void setupBallTouch() {
        ball.setOnTouchListener(new View.OnTouchListener() {
            private float dx, dy;
            private int sx, sy;
            private long t;
            private boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (snapping) return true;
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = e.getRawX(); dy = e.getRawY();
                        sx = params.x; sy = params.y;
                        t = System.currentTimeMillis();
                        moved = false;
                        if (expanded) togglePanel();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int mx = (int) (e.getRawX() - dx);
                        int my = (int) (e.getRawY() - dy);
                        if (Math.abs(mx) > 6 || Math.abs(my) > 6) moved = true;
                        params.x = sx + mx;
                        params.y = sy + my;
                        clamp();
                        wm.updateViewLayout(root, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!moved && System.currentTimeMillis() - t < 300) {
                            togglePanel();
                        } else if (moved) {
                            snap();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void snap() {
        snapping = true;
        int bw = ball.getWidth();
        if (bw == 0) bw = dp(64);
        int target = (params.x + bw / 2 < screenW / 2) ? 0 : screenW - bw;
        ValueAnimator a = ValueAnimator.ofInt(params.x, target);
        a.setDuration(280).setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> { params.x = (int) anim.getAnimatedValue(); wm.updateViewLayout(root, params); });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) { snapping = false; }
        });
        a.start();
    }

    private void clamp() {
        int bw = ball.getWidth(), bh = ball.getHeight();
        if (bw == 0) bw = dp(64);
        if (bh == 0) bh = dp(64);
        params.x = Math.max(0, Math.min(params.x, screenW - bw));
        params.y = Math.max(statusBarH, Math.min(params.y, screenH - bh - navBarH));
    }

    /* ==================== 面板开关 ==================== */

    private void togglePanel() {
        if (expanded) {
            panel.animate().alpha(0f).scaleX(0.3f).scaleY(0.3f).setDuration(220)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> { panel.setVisibility(View.GONE); expanded = false; }).start();
            ball.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        } else {
            panel.setVisibility(View.VISIBLE);
            expanded = true;
            panel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(320)
                    .setInterpolator(new OvershootInterpolator(1.2f)).start();
            ball.animate().scaleX(0.85f).scaleY(0.85f).setDuration(200).start();
        }
    }

    /* ==================== 三页 Tab ==================== */

    private void setupTabs() {
        TextView tab1 = panel.findViewById(R.id.tab_functions);
        TextView tab2 = panel.findViewById(R.id.tab_settings);
        TextView tab3 = panel.findViewById(R.id.tab_theme);
        View.OnClickListener ocl = v -> {
            int idx = (v == tab1) ? 0 : (v == tab2) ? 1 : 2;
            showPage(idx);
        };
        tab1.setOnClickListener(ocl);
        tab2.setOnClickListener(ocl);
        tab3.setOnClickListener(ocl);
    }

    private void showPage(int idx) {
        currentPage = idx;
        pageFunctions.setVisibility(idx == 0 ? View.VISIBLE : View.GONE);
        pageSettings.setVisibility(idx == 1 ? View.VISIBLE : View.GONE);
        pageTheme.setVisibility(idx == 2 ? View.VISIBLE : View.GONE);
        TextView t1 = panel.findViewById(R.id.tab_functions);
        TextView t2 = panel.findViewById(R.id.tab_settings);
        TextView t3 = panel.findViewById(R.id.tab_theme);
        int primary = NyanConfig.themePrimary(this);
        t1.setTextColor(idx == 0 ? primary : 0xFF888888);
        t2.setTextColor(idx == 1 ? primary : 0xFF888888);
        t3.setTextColor(idx == 2 ? primary : 0xFF888888);
    }

    /* ==================== 功能页 ==================== */

    private void setupFunctionPage() {
        Switch swNya = pageFunctions.findViewById(R.id.sw_add_nya);
        Switch swMe = pageFunctions.findViewById(R.id.sw_me);
        Switch swYou = pageFunctions.findViewById(R.id.sw_you);

        swNya.setChecked(NyanConfig.isAddNya(this));
        swMe.setChecked(NyanConfig.isMe(this));
        swYou.setChecked(NyanConfig.isYou(this));

        swNya.setOnCheckedChangeListener((b, c) -> NyanConfig.setAddNya(this, c));
        swMe.setOnCheckedChangeListener((b, c) -> NyanConfig.setMe(this, c));
        swYou.setOnCheckedChangeListener((b, c) -> NyanConfig.setYou(this, c));
    }

    /* ==================== 设置页（互斥特效） ==================== */

    private void setupSettingPage() {
        Switch swSnow = pageSettings.findViewById(R.id.sw_snow);
        Switch swMeteor = pageSettings.findViewById(R.id.sw_meteor);

        swSnow.setChecked(NyanConfig.isSnow(this));
        swMeteor.setChecked(NyanConfig.isMeteor(this));

        swSnow.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                // 开雪花 -> 自动关流星
                NyanConfig.setSnow(this, true);
                swMeteor.setChecked(false);
                effectOverlay.startSnow();
            } else {
                NyanConfig.setSnow(this, false);
                if (!NyanConfig.isMeteor(this)) effectOverlay.stopAll();
            }
        });

        swMeteor.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                // 开流星 -> 自动关雪花
                NyanConfig.setMeteor(this, true);
                swSnow.setChecked(false);
                effectOverlay.startMeteor();
            } else {
                NyanConfig.setMeteor(this, false);
                if (!NyanConfig.isSnow(this)) effectOverlay.stopAll();
            }
        });

        // 恢复上次状态
        if (NyanConfig.isSnow(this)) effectOverlay.startSnow();
        else if (NyanConfig.isMeteor(this)) effectOverlay.startMeteor();
    }

    /* ==================== 主题页 ==================== */

    private void setupThemePage() {
        View card0 = pageTheme.findViewById(R.id.theme_0);
        View card1 = pageTheme.findViewById(R.id.theme_1);
        View card2 = pageTheme.findViewById(R.id.theme_2);
        View.OnClickListener ocl = v -> {
            int t = (v == card0) ? 0 : (v == card1) ? 1 : 2;
            NyanConfig.setTheme(this, t);
            applyThemeColors();
            updateThemeSelection();
        };
        card0.setOnClickListener(ocl);
        card1.setOnClickListener(ocl);
        card2.setOnClickListener(ocl);
        updateThemeSelection();
    }

    private void updateThemeSelection() {
        int t = NyanConfig.getTheme(this);
        View card0 = pageTheme.findViewById(R.id.theme_0);
        View card1 = pageTheme.findViewById(R.id.theme_1);
        View card2 = pageTheme.findViewById(R.id.theme_2);
        card0.setAlpha(t == 0 ? 1f : 0.5f);
        card1.setAlpha(t == 1 ? 1f : 0.5f);
        card2.setAlpha(t == 2 ? 1f : 0.5f);
    }

    private void applyThemeColors() {
        int primary = NyanConfig.themePrimary(this);
        // 悬浮球颜色跟随主题
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(primary);
        ball.setBackground(d);
        ballIcon.setBackground(null);
    }

    /* ==================== 工具 ==================== */

    private void computeMetrics() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowMetrics m = wm.getCurrentWindowMetrics();
            android.graphics.Rect b = m.getBounds();
            screenW = b.width(); screenH = b.height();
        } else {
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            screenW = p.x; screenH = p.y;
        }
        int sb = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (sb > 0) statusBarH = getResources().getDimensionPixelSize(sb);
        int nb = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (nb > 0) navBarH = getResources().getDimensionPixelSize(nb);
    }

    private int getLayoutType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    /* ==================== 通知（前台服务） ==================== */

    private Notification buildNotif() {
        return new NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗运行中喵~")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "本喵助手", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) { return START_STICKY; }

    @Override
    public void onDestroy() {
        if (root != null && wm != null) {
            try { wm.removeView(root); } catch (Exception ignored) {}
            root = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

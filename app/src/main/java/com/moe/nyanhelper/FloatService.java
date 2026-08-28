package com.moe.nyanhelper;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

public class FloatService extends Service {

    private WindowManager wm;
    private View ball, panel;
    private WindowManager.LayoutParams ballParams;
    private int screenW, screenH;
    private boolean expanded = false;

    // 特效层（叠加在悬浮球所在窗口内）
    private EffectOverlay effectOverlay;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        computeScreen();

        ball = LayoutInflater.from(this).inflate(R.layout.float_ball, null);
        ball.findViewById(R.id.ballImage).setClipToOutline(true);

        ballParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        ballParams.gravity = Gravity.TOP | Gravity.START;
        ballParams.x = screenW - dp(80);
        ballParams.y = screenH / 3;

        ball.setOnTouchListener(new BallTouchListener());
        wm.addView(ball, ballParams);

        // 特效层（覆盖全屏，独立于悬浮球拖动）
        effectOverlay = new EffectOverlay(this);
    }

    private void computeScreen() {
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        screenW = p.x;
        screenH = p.y;
    }

    private void togglePanel() {
        if (expanded) {
            hidePanel();
        } else {
            showPanel();
        }
    }

    private void showPanel() {
        if (panel != null) return;
        panel = LayoutInflater.from(this).inflate(R.layout.float_panel, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dp(200),
                WindowManager.LayoutParams.WRAP_CONTENT,
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = Math.max(dp(8), ballParams.x - dp(60));
        params.y = Math.min(Math.max(ballParams.y + dp(60), dp(120)), screenH - dp(400));

        panel.findViewById(R.id.tabFeatures).setOnClickListener(v -> showFeatures());
        panel.findViewById(R.id.tabSettings).setOnClickListener(v -> showSettings());
        panel.findViewById(R.id.tabTheme).setOnClickListener(v -> showTheme());
        panel.findViewById(R.id.panelClose).setOnClickListener(v -> {
            hidePanel();
            stopSelf();
        });

        wm.addView(panel, params);
        expanded = true;
        ball.setAlpha(0.85f);
    }

    private void hidePanel() {
        if (panel != null) {
            wm.removeView(panel);
            panel = null;
        }
        expanded = false;
        ball.setAlpha(1f);
    }

    // ===== 三个页面（在面板内切换内容）=====

    private void showFeatures() {
        setPanelContent(R.layout.panel_features);
        bindFeatures();
    }

    private void showSettings() {
        setPanelContent(R.layout.panel_settings);
        bindSettings();
    }

    private void showTheme() {
        setPanelContent(R.layout.panel_theme);
        bindTheme();
    }

    private void setPanelContent(int layoutRes) {
        if (panel == null) return;
        android.view.ViewGroup content = panel.findViewById(R.id.panelContent);
        content.removeAllViews();
        View v = LayoutInflater.from(this).inflate(layoutRes, content, false);
        content.addView(v);
    }

    private void bindFeatures() {
        android.widget.Switch sw1 = panel.findViewById(R.id.swAddNya);
        android.widget.Switch sw2 = panel.findViewById(R.id.swReplaceYou);
        android.widget.Switch sw3 = panel.findViewById(R.id.swReplaceMe);
        if (sw1 != null) {
            sw1.setChecked(NyanConfig.isAddNya(this));
            sw1.setOnCheckedChangeListener((b, c) -> NyanConfig.setAddNya(this, c));
        }
        if (sw2 != null) {
            sw2.setChecked(NyanConfig.isReplaceYou(this));
            sw2.setOnCheckedChangeListener((b, c) -> NyanConfig.setReplaceYou(this, c));
        }
        if (sw3 != null) {
            sw3.setChecked(NyanConfig.isReplaceMe(this));
            sw3.setOnCheckedChangeListener((b, c) -> NyanConfig.setReplaceMe(this, c));
        }
    }

    private void bindSettings() {
        android.widget.Switch swSnow = panel.findViewById(R.id.swSnow);
        android.widget.Switch swMeteor = panel.findViewById(R.id.swMeteor);
        if (swSnow != null) {
            swSnow.setChecked(NyanConfig.isSnow(this));
            swSnow.setOnCheckedChangeListener((b, c) -> {
                NyanConfig.setSnow(this, c);
                if (c) effectOverlay.startSnow();
                else effectOverlay.stopSnow();
                refreshSettings();
            });
        }
        if (swMeteor != null) {
            swMeteor.setChecked(NyanConfig.isMeteor(this));
            swMeteor.setOnCheckedChangeListener((b, c) -> {
                NyanConfig.setMeteor(this, c);
                if (c) effectOverlay.startMeteor();
                else effectOverlay.stopMeteor();
                refreshSettings();
            });
        }
    }

    private void refreshSettings() {
        // 互斥后刷新 UI
        android.widget.Switch swSnow = panel.findViewById(R.id.swSnow);
        android.widget.Switch swMeteor = panel.findViewById(R.id.swMeteor);
        if (swSnow != null) swSnow.setChecked(NyanConfig.isSnow(this));
        if (swMeteor != null) swMeteor.setChecked(NyanConfig.isMeteor(this));
    }

    private void bindTheme() {
        View.OnClickListener pick = v -> {
            int t = (int) v.getTag();
            NyanConfig.setTheme(this, t);
            android.widget.Toast.makeText(this, "主题已切换喵~", android.widget.Toast.LENGTH_SHORT).show();
        };
        View pink = panel.findViewById(R.id.themePink);
        View green = panel.findViewById(R.id.themeGreen);
        View purple = panel.findViewById(R.id.themePurple);
        if (pink != null) { pink.setTag(0); pink.setOnClickListener(pick); }
        if (green != null) { green.setTag(1); green.setOnClickListener(pick); }
        if (purple != null) { purple.setTag(2); purple.setOnClickListener(pick); }
    }

    // ===== 拖动 + 贴边 =====

    private class BallTouchListener implements View.OnTouchListener {
        private float downX, downY;
        private int startX, startY;
        private long downTime;
        private boolean moved = false;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = e.getRawX(); downY = e.getRawY();
                    startX = ballParams.x; startY = ballParams.y;
                    downTime = System.currentTimeMillis();
                    moved = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (e.getRawX() - downX);
                    int dy = (int) (e.getRawY() - downY);
                    if (Math.abs(dx) > dp(4) || Math.abs(dy) > dp(4)) moved = true;
                    ballParams.x = startX + dx;
                    ballParams.y = startY + dy;
                    clamp();
                    wm.updateViewLayout(ball, ballParams);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!moved && System.currentTimeMillis() - downTime < 300) {
                        togglePanel();
                    } else {
                        snap();
                    }
                    return true;
            }
            return false;
        }
    }

    private void clamp() {
        int bw = ball.getWidth(), bh = ball.getHeight();
        if (bw == 0) bw = dp(64);
        if (bh == 0) bh = dp(64);
        ballParams.x = Math.max(0, Math.min(ballParams.x, screenW - bw));
        ballParams.y = Math.max(0, Math.min(ballParams.y, screenH - bh));
    }

    private void snap() {
        int bw = ball.getWidth();
        if (bw == 0) bw = dp(64);
        final int target = (ballParams.x + bw / 2 < screenW / 2) ? 0 : screenW - bw;
        ValueAnimator a = ValueAnimator.ofInt(ballParams.x, target);
        a.setDuration(260).setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> {
            ballParams.x = (int) anim.getAnimatedValue();
            wm.updateViewLayout(ball, ballParams);
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // 贴边完成
            }
        });
        a.start();
    }

    private int getType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (effectOverlay != null) effectOverlay.destroy();
        if (panel != null) wm.removeView(panel);
        if (ball != null) wm.removeView(ball);
    }
}

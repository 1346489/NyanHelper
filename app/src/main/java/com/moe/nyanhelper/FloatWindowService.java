package com.moe.nyanhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Switch;

import androidx.core.app.NotificationCompat;

import android.animation.ValueAnimator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class FloatWindowService extends Service {

    private static final String CHANNEL_ID = "nyan_helper_channel";
    private WindowManager wm;
    private View floatView;
    private View ballView;
    private View panelView;
    private WindowManager.LayoutParams params;

    private int screenW = 1080, screenH = 1920;
    private int statusBarHeight = 0, navBarHeight = 0;
    private boolean panelExpanded = false;
    private boolean isSnapping = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotification());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        computeScreenAndInsets();

        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        ballView = floatView.findViewById(R.id.float_ball);
        panelView = floatView.findViewById(R.id.float_panel);

        panelView.setVisibility(View.GONE);
        panelView.setAlpha(0f);
        panelView.setScaleX(0.3f);
        panelView.setScaleY(0.3f);

        int ballSize = dp(52);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = screenW - ballSize - dp(20);
        params.y = dp(200);

        wm.addView(floatView, params);
        setupTouch();
        setupSwitches();
    }

    private void setupTouch() {
        ballView.setOnTouchListener(new View.OnTouchListener() {

            private float downX, downY;
            private int startX, startY;
            private long downTime;
            private boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (isSnapping) return true;

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = params.x;
                        startY = params.y;
                        downTime = System.currentTimeMillis();
                        moved = false;
                        if (panelExpanded) togglePanel();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - downX);
                        int dy = (int) (e.getRawY() - downY);
                        if (Math.abs(dx) > 6 || Math.abs(dy) > 6) moved = true;
                        params.x = startX + dx;
                        params.y = startY + dy;
                        clampPosition();
                        wm.updateViewLayout(floatView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!moved && System.currentTimeMillis() - downTime < 300) {
                            togglePanel();
                        } else if (moved) {
                            snapToEdge();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void setupSwitches() {
        Switch sw1 = floatView.findViewById(R.id.sw_add_nya);
        Switch sw2 = floatView.findViewById(R.id.sw_me);
        Switch sw3 = floatView.findViewById(R.id.sw_you);

        if (sw1 != null) {
            sw1.setChecked(NyanConfig.isAddNya(this));
            sw1.setOnCheckedChangeListener((b, c) -> NyanConfig.setAddNya(this, c));
        }
        if (sw2 != null) {
            sw2.setChecked(NyanConfig.isMe(this));
            sw2.setOnCheckedChangeListener((b, c) -> NyanConfig.setMe(this, c));
        }
        if (sw3 != null) {
            sw3.setChecked(NyanConfig.isYou(this));
            sw3.setOnCheckedChangeListener((b, c) -> NyanConfig.setYou(this, c));
        }
    }

    private void togglePanel() {
        if (panelExpanded) {
            panelView.animate()
                    .alpha(0f).scaleX(0.3f).scaleY(0.3f)
                    .setDuration(220)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        panelView.setVisibility(View.GONE);
                        panelExpanded = false;
                    })
                    .start();
            ballView.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
        } else {
            panelView.setVisibility(View.VISIBLE);
            panelExpanded = true;
            panelView.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(320)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
            ballView.animate().scaleX(0.85f).scaleY(0.85f).setDuration(200).start();
        }
    }

    private void snapToEdge() {
        isSnapping = true;
        int bw = ballView.getWidth();
        if (bw == 0) bw = dp(52);
        int cx = params.x + bw / 2;
        int targetX = (cx < screenW / 2) ? 0 : screenW - bw;

        ValueAnimator anim = ValueAnimator.ofInt(params.x, targetX);
        anim.setDuration(280);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            params.x = (int) animation.getAnimatedValue();
            wm.updateViewLayout(floatView, params);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isSnapping = false;
            }
        });
        anim.start();
    }

    private void clampPosition() {
        int bw = ballView.getWidth();
        int bh = ballView.getHeight();
        if (bw == 0) bw = dp(52);
        if (bh == 0) bh = dp(52);
        params.x = Math.max(0, Math.min(params.x, screenW - bw));
        params.y = Math.max(statusBarHeight, Math.min(params.y, screenH - bh - navBarHeight));
    }

    private void computeScreenAndInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics m = wm.getCurrentWindowMetrics();
            Rect b = m.getBounds();
            screenW = b.width();
            screenH = b.height();
        } else {
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            screenW = p.x;
            screenH = p.y;
        }
        int sb = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (sb > 0) statusBarHeight = getResources().getDimensionPixelSize(sb);
        int nb = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (nb > 0) navBarHeight = getResources().getDimensionPixelSize(nb);
    }

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗服务运行中喵~")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "本喵助手", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (floatView != null && wm != null) {
            try { wm.removeView(floatView); } catch (Exception ignored) {}
            floatView = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

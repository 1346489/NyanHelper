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
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import androidx.core.app.NotificationCompat;

public class FloatWindowService extends Service {

    private static final String CHANNEL = "nyan_channel";
    private WindowManager wm;
    private View root, ball, panel;
    private EffectView effectView;
    private WindowManager.LayoutParams params;
    private int sw = 1080, sh = 1920, sbh = 0, nbh = 0;
    private boolean expanded = false, snapping = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotif());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        compute();

        root = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        ball = root.findViewById(R.id.float_ball);
        panel = root.findViewById(R.id.float_panel);
        effectView = root.findViewById(R.id.effectView);
        panel.setVisibility(View.GONE);
        panel.setAlpha(0f);
        panel.setScaleX(0.3f);
        panel.setScaleY(0.3f);

        int size = dp(60);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        // 悬浮球初始位置
        ball.setX(sw - size - dp(20));
        ball.setY(dp(220));

        wm.addView(root, params);
        setupTouch();
        refreshEffect();
    }

    private void setupTouch() {
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
                        sx = (int) ball.getX(); sy = (int) ball.getY();
                        t = System.currentTimeMillis();
                        moved = false;
                        if (expanded) togglePanel();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int mx = (int) (e.getRawX() - dx);
                        int my = (int) (e.getRawY() - dy);
                        if (Math.abs(mx) > 6 || Math.abs(my) > 6) moved = true;
                        ball.setX(sx + mx);
                        ball.setY(sy + my);
                        clamp();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!moved && System.currentTimeMillis() - t < 300) togglePanel();
                        else if (moved) snap();
                        return true;
                }
                return false;
            }
        });
    }

    private void togglePanel() {
        if (expanded) {
            panel.animate().alpha(0f).scaleX(0.3f).scaleY(0.3f).setDuration(220)
                    .withEndAction(() -> { panel.setVisibility(View.GONE); expanded = false; }).start();
        } else {
            panel.setVisibility(View.VISIBLE);
            expanded = true;
            panel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(320)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
        }
    }

    private void snap() {
        snapping = true;
        int bw = ball.getWidth();
        if (bw == 0) bw = dp(60);
        int target = ((int) ball.getX() + bw / 2 < sw / 2) ? 0 : sw - bw;
        ValueAnimator a = ValueAnimator.ofInt((int) ball.getX(), target);
        a.setDuration(280).setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(anim -> ball.setX((int) anim.getAnimatedValue()));
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) { snapping = false; }
        });
        a.start();
    }

    private void clamp() {
        int bw = ball.getWidth(), bh = ball.getHeight();
        if (bw == 0) bw = dp(60);
        if (bh == 0) bh = dp(60);
        ball.setX(Math.max(0, Math.min((int) ball.getX(), sw - bw)));
        ball.setY(Math.max(sbh, Math.min((int) ball.getY(), sh - bh - nbh)));
    }

    private void compute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowMetrics m = wm.getCurrentWindowMetrics();
            Rect b = m.getBounds();
            sw = b.width(); sh = b.height();
        } else {
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            sw = p.x; sh = p.y;
        }
        int sb = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (sb > 0) sbh = getResources().getDimensionPixelSize(sb);
        int nb = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (nb > 0) nbh = getResources().getDimensionPixelSize(nb);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void refreshEffect() {
        if (effectView == null) return;
        boolean snow = NyanConfig.isSnow(this);
        boolean meteor = NyanConfig.isMeteor(this);
        if (snow) effectView.setMode(EffectView.MODE_SNOW);
        else if (meteor) effectView.setMode(EffectView.MODE_METEOR);
        else effectView.setMode(EffectView.MODE_NONE);
    }

    private Notification buildNotif() {
        return new NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle("本喵助手").setContentText("悬浮窗服务运行中喵~")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setPriority(NotificationCompat.PRIORITY_LOW).build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "本喵助手", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private int getType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "UPDATE_EFFECT".equals(intent.getAction())) {
            refreshEffect();
        }
        return START_STICKY;
    }

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

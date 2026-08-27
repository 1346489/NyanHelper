package com.moe.nyanhelper;

import android.app.ActivityManager;
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
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

public class FloatWindowService extends Service {

    private static final String CHANNEL_ID = "nyan_helper_channel";
    private WindowManager wm;
    private View floatView;
    private WindowManager.LayoutParams params;

    private int statusBarHeight = 0;
    private int navBarHeight = 0;
    private int screenW = 1080;
    private int screenH = 1920;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotification());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        computeScreenAndInsets();

        // ===== 圆形悬浮窗（inflate XML 布局）=====
        floatView = LayoutInflater.from(this).inflate(R.layout.float_view, null);

        // ===== 布局参数 =====
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = dp(120);

        wm.addView(floatView, params);

        // ===== 拖拽 + 点击区分 =====
        floatView.setOnTouchListener(new View.OnTouchListener() {

            private float downX, downY;
            private int startX, startY;
            private long downTime;
            private boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getRawX();
                        downY = e.getRawY();
                        startX = params.x;
                        startY = params.y;
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - downX);
                        int dy = (int) (e.getRawY() - downY);
                        if (Math.abs(dx) > 6 || Math.abs(dy) > 6) {
                            moved = true;
                        }
                        params.x = startX + dx;
                        params.y = startY + dy;

                        // 边界限制
                        int viewW = floatView.getWidth();
                        int viewH = floatView.getHeight();
                        if (viewW == 0) viewW = dp(56);
                        if (viewH == 0) viewH = dp(56);

                        params.x = Math.max(0, Math.min(params.x, screenW - viewW));
                        params.y = Math.max(statusBarHeight, Math.min(params.y, screenH - viewH - navBarHeight));

                        wm.updateViewLayout(floatView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved && System.currentTimeMillis() - downTime < 300) {
                            // 点击 → 打开/回到主界面
                            openApp();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    // ===== 点击悬浮窗 → 打开/回到 MainActivity =====
    private void openApp() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null) {
            for (ActivityManager.AppTask task : am.getAppTasks()) {
                if (task != null) {
                    task.moveToFront();
                    break;
                }
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ===== 屏幕/Insets 计算 =====
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (floatView != null && wm != null) {
            try {
                wm.removeView(floatView);
            } catch (Exception ignored) {
            }
            floatView = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗服务运行中")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "本喵助手前台服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}

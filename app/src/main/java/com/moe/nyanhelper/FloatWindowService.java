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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class FloatWindowService extends Service {

    private static final String CHANNEL_ID = "nyan_helper_channel";
    private WindowManager wm;
    private View floatView;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1, buildNotification());

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        // ===== 悬浮窗内容 =====
        TextView tv = new TextView(this);
        tv.setText("🐱 本喵助手");
        tv.setTextSize(14);
        tv.setTextColor(0xFFFFFFFF);
        tv.setBackgroundColor(0xAAFF69B4);
        tv.setPadding(30, 20, 30, 20);
        floatView = tv;

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
        params.x = 100;
        params.y = 300;

        wm.addView(floatView, params);

        // ===== 拖拽 + 边界限制 =====
        int statusBarHeight = getStatusBarHeight();
        int navBarHeight = getNavBarHeight();
        int[] screenSize = getScreenSize();
        final int[] lastTouchX = {0}, lastTouchY = {0};
        final int[] lastParamX = {params.x}, lastParamY = {params.y};

        floatView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX[0] = (int) event.getRawX();
                    lastTouchY[0] = (int) event.getRawY();
                    lastParamX[0] = params.x;
                    lastParamY[0] = params.y;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastTouchX[0];
                    int dy = (int) event.getRawY() - lastTouchY[0];
                    params.x = lastParamX[0] + dx;
                    params.y = lastParamY[0] + dy;

                    // 边界限制
                    int viewW = floatView.getWidth();
                    int viewH = floatView.getHeight();
                    if (viewW == 0) viewW = 200; // 初始未测量时的估计值
                    if (viewH == 0) viewH = 80;

                    params.x = Math.max(0, Math.min(params.x, screenSize[0] - viewW));
                    params.y = Math.max(statusBarHeight, Math.min(params.y, screenSize[1] - viewH - navBarHeight));

                    wm.updateViewLayout(floatView, params);
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;
            }
            return false;
        });
    }

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    private int[] getScreenSize() {
        final int[] size = {1080, 1920};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = wm.getCurrentWindowMetrics().getBounds();
            size[0] = bounds.width();
            size[1] = bounds.height();
        } else {
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            size[0] = p.x;
            size[1] = p.y;
        }
        return size;
    }

    private int getStatusBarHeight() {
        int res = 0;
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) res = getResources().getDimensionPixelSize(id);
        return res;
    }

    private int getNavBarHeight() {
        int res = 0;
        int id = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) res = getResources().getDimensionPixelSize(id);
        return res;
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

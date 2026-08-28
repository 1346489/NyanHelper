package com.moe.nyanhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 悬浮窗服务：
 * - 显示可拖动的悬浮球/面板（float_window.xml）
 * - 面板含 3 个选项：功能 / 设置 / 主题（点击跳转对应 Activity）
 * - 承载 SnowMeteorView 特效层，响应设置页雪花/流星开关
 * - Android 8+ 前台服务 + TYPE_APPLICATION_OVERLAY
 */
public class FloatService extends Service {

    private static final String CHANNEL_ID = "nyan_float_channel";
    private static final String ACTION_REFRESH = "REFRESH_EFFECT";

    private WindowManager wm;
    private View floatView;
    private FrameLayout effectLayer;
    private SnowMeteorView effectView;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        // 无悬浮窗权限则退出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf(); return;
        }

        startForeground(1, buildNotification());
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        effectLayer = floatView.findViewById(R.id.effectLayer);

        effectView = new SnowMeteorView(this);
        effectLayer.addView(effectView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        effectView.refreshConfig(NyanConfig.isSnow(this), NyanConfig.isMeteor(this));

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 60;
        params.y = 200;

        setupDragAndTabs();
        try {
            wm.addView(floatView, params);
            NyanConfig.setServiceRunning(this, true);
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void setupDragAndTabs() {
        // 拖动（拖动时不触发点击）
        floatView.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY, startX, startY;
            long downTime;
            boolean moved;
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = e.getRawX(); downY = e.getRawY();
                        startX = params.x; startY = params.y;
                        downTime = System.currentTimeMillis(); moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(e.getRawX() - downX) > 8 || Math.abs(e.getRawY() - downY) > 8) moved = true;
                        params.x = (int)(startX + e.getRawX() - downX);
                        params.y = (int)(startY + e.getRawY() - downY);
                        wm.updateViewLayout(floatView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return moved || (System.currentTimeMillis() - downTime > 200);
                }
                return false;
            }
        });

        // 3 个选项：功能 / 设置 / 主题
        bindTab(R.id.tabFeatures, FeaturesActivity.class);
        bindTab(R.id.tabSettings, SettingsActivity.class);
        bindTab(R.id.tabTheme, ThemeActivity.class);

        // 隐藏
        TextView tabHide = floatView.findViewById(R.id.tabHide);
        if (tabHide != null) {
            tabHide.setOnClickListener(v -> {
                if (floatView != null) wm.removeView(floatView);
                floatView = null;
                NyanConfig.setServiceRunning(this, false);
                stopSelf();
            });
        }
    }

    private void bindTab(int id, Class<?> activityClass) {
        TextView tab = floatView.findViewById(id);
        if (tab == null) return;
        tab.setOnClickListener(v -> {
            Intent intent = new Intent(this, activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_REFRESH.equals(intent.getAction()) && effectView != null) {
            effectView.refreshConfig(NyanConfig.isSnow(this), NyanConfig.isMeteor(this));
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (effectView != null) effectView.stop();
        if (floatView != null && wm != null) {
            try { wm.removeView(floatView); } catch (Exception ignored) {}
        }
        NyanConfig.setServiceRunning(this, false);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID, "本喵悬浮窗", NotificationManager.IMPORTANCE_LOW);
                ch.setDescription("本喵助手悬浮窗与特效运行提示");
                nm.createNotificationChannel(ch);
            }
        }
    }

    private Notification buildNotification() {
        createChannel();
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return b.setContentTitle("本喵助手")
                .setContentText("悬浮窗与特效运行中")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .build();
    }
}

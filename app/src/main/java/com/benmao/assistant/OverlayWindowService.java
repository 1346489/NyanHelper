package com.benmao.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
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

    private int overlayType;   // 悬浮窗类型（兼容 API26 以下）
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
        // Android 14+ 必须传前台服务类型，否则 MissingForegroundServiceTypeException 崩溃
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
                        openMenuWithAnimation();
                    }
                    return true;
            }
            return false;
        });

        // 捕获悬浮窗权限未真正授予导致的崩溃（国产 ROM 常见）
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
    

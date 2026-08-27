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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.core.app.NotificationCompat;

public class FloatWindowService extends Service {

    private WindowManager wm;
    private View panelView;
    private View ballView;
    private boolean expanded = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showBall();
        showPanel();
        collapse(false);
    }

    private Notification createNotification() {
        String chan = "nyan_chan";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(
                    chan, "本喵悬浮窗", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
        return new NotificationCompat.Builder(this, chan)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗运行中喵~")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private int getWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void showBall() {
        ballView = LayoutInflater.from(this).inflate(R.layout.float_ball, null);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                dp(72), dp(72),
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = dp(24);
        p.y = dp(180);
        wm.addView(ballView, p);
        ballView.findViewById(R.id.ballBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(true);
            }
        });
    }

    private void showPanel() {
        panelView = LayoutInflater.from(this).inflate(R.layout.float_panel, null);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                dp(300),
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = dp(40);
        p.y = dp(120);
package com.moe.nyanhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class FloatWindowService extends Service {

    private static final String CHANNEL_ID = "nyan_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("本喵助手")
                .setContentText("悬浮窗运行中喵~")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "本喵助手", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}

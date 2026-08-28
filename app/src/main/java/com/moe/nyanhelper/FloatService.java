package com.moe.nyanhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class FloatService extends Service {

    private static final String CHANNEL_ID = "nyan_float";
    private static final String ACTION_REFRESH_EFFECT = "com.moe.nyanhelper.action.REFRESH_EFFECT";

    private WindowManager wm;
    private View floatView;
    private SnowMeteorView effectView;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        createChannel();
        startForeground(1, buildNotification());

        LayoutInflater inflater = LayoutInflater.from(this);
        floatView = inflater.inflate(R.layout.float_window, null);

        effectView = floatView.findViewById(R.id.effectLayer);

        TextView tabHide = floatView.findViewById(R.id.tabHide);
        TextView tabFeatures = floatView.findViewById(R.id.tabFeatures);
        TextView tabSettings = floatView.findViewById(R.id.tabSettings);
        TextView tabTheme = floatView.findViewById(R.id.tabTheme);
        TextView ball = floatView.findViewById(R.id.float_ball);

        if (ball != null) {
            ball.setOnClickListener(v ->
                    Toast.makeText(this, "本喵在～", Toast.LENGTH_SHORT).show()
            );
        }

        if (tabFeatures != null) {
            tabFeatures.setOnClickListener(v -> {
                Intent i = new Intent(this, FeaturesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            });
        }

        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> {
                Intent i = new Intent(this, SettingsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            });
        }

        if (tabTheme != null) {
            tabTheme.setOnClickListener(v -> {
                Intent i = new Intent(this, ThemeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            });
        }

        if (tabHide != null) {
            tabHide.setOnClickListener(v -> stopSelf());
        }

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                120, 120,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 30;
        lp.y = 150;

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        try {
            wm.addView(floatView, lp);
        } catch (Exception e) {
            stopSelf();
            return;
        }

        if (effectView != null) {
            effectView.refreshConfig(NyanConfig.isSnow(this), NyanConfig.isMeteor(this));
        }

        NyanConfig.setServiceRunning(this, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_REFRESH_EFFECT.equals(intent.getAction())) {
            if (effectView != null) {
                effectView.refreshConfig(NyanConfig.isSnow(this), NyanConfig.isMeteor(this));
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (floatView != null && wm != null) {
            try {
                wm.removeView(floatView);
            } catch (Exception ignored) {
            }
        }

        if (effectView != null) {
            effectView.stopEffect();
            effectView = null;
        }

        NyanConfig.setServiceRunning(this, false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "本喵悬浮窗",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(ch);
            }
        }
    }

    private Notification buildNotification() {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                i,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return b.setContentTitle("本喵助手")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(pi)
                .build();
    }
}

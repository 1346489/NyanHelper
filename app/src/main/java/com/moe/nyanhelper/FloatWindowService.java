package com.moe.nyanhelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 悬浮窗服务：显示 120x120 的小面板，含 3 个选项（功能/设置/主题）+
 * 雪花/流星特效层（SnowMeteorView）。
 *
 * 类名统一为 FloatWindowService（Manifest 里也对应 .FloatWindowService）。
 */
public class FloatWindowService extends Service {

    public static final String ACTION_REFRESH_EFFECT = "com.moe.nyanhelper.REFRESH_EFFECT";
    private static final String CHANNEL_ID = "nyan_float";

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

        // 这些 id 必须在 float_window.xml 里存在，否则 cannot find symbol
        effectView = floatView.findViewById(R.id.effectView);
        TextView ball = floatView.findViewById(R.id.float_ball);
        TextView tabFeatures = floatView.findViewById(R.id.tabFeatures);
        TextView tabSettings = floatView.findViewById(R.id.tabSettings);
        TextView tabTheme = floatView.findViewById(R.id.tabTheme);

        if (ball != null) {
            ball.setOnClickListener(v ->
                    Toast.makeText(this, "本喵在～", Toast.LENGTH_SHORT).show());
        }

        // 3 个选项：点击跳转对应 Activity
        tabFeatures.setOnClickListener(v -> openActivity(FeaturesActivity.class));
        tabSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));
        tabTheme.setOnClickListener(v -> openActivity(ThemeActivity.class));

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                120, 120, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
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

    private void openActivity(Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 设置页开关变化 → 刷新特效
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
            try { wm.removeView(floatView); } catch (Exception ignored) {}
        }
        if (effectView != null) {
            effectView.refreshConfig(false, false);
        }
        NyanConfig.setServiceRunning(this, false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "本喵悬浮窗", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
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

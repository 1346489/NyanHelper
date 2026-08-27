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
        wm.addView(panelView, p);

        Switch swTail = panelView.findViewById(R.id.switchTail);
        Switch swMe = panelView.findViewById(R.id.switchMe);
        Switch swYou = panelView.findViewById(R.id.switchYou);

        swTail.setChecked(Prefs.addMeowTail(this));
        swMe.setChecked(Prefs.replaceMe(this));
        swYou.setChecked(Prefs.replaceYou(this));

        swTail.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                Prefs.setAddMeowTail(FloatWindowService.this, isChecked);
            }
        });
        swMe.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                Prefs.setReplaceMe(FloatWindowService.this, isChecked);
            }
        });
        swYou.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                Prefs.setReplaceYou(FloatWindowService.this, isChecked);
            }
        });
        panelView.findViewById(R.id.closePanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse(true);
            }
        });
    }

    private void expand(boolean anim) {
        if (expanded) {
            return;
        }
        expanded = true;
        panelView.setVisibility(View.VISIBLE);
        ballView.setVisibility(View.GONE);
    }

    private void collapse(boolean anim) {
        if (!expanded) {
            return;
        }
        expanded = false;
        panelView.setVisibility(View.GONE);
        ballView.setVisibility(View.VISIBLE);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroy() {
        if (panelView != null) {
            wm.removeView(panelView);
        }
        if (ballView != null) {
            wm.removeView(ballView);
        }
        super.onDestroy();
    }
}

package com.moe.nyanhelper;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatWindowService extends Service {

    private WindowManager wm;
    private WindowManager.LayoutParams ballParams;
    private View floatView;
    private ImageButton float_ball;
    private LinearLayout float_panel;
    private View effectView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        showFloatBall();
    }

    private void showFloatBall() {
        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);

        float_ball = floatView.findViewById(R.id.float_ball);
        float_panel = floatView.findViewById(R.id.float_panel);
        effectView = floatView.findViewById(R.id.effectView);

        ballParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        ballParams.gravity = Gravity.TOP | Gravity.START;
        ballParams.x = 0;
        ballParams.y = 300;

        wm.addView(floatView, ballParams);

        // 点击球 → 显示/隐藏面板
        float_ball.setOnClickListener(v -> {
            if (float_panel.getVisibility() == View.VISIBLE) {
                float_panel.setVisibility(View.GONE);
            } else {
                float_panel.setVisibility(View.VISIBLE);
            }
        });

        // 隐藏悬浮
        TextView tabHide = floatView.findViewById(R.id.tabHide);
        if (tabHide != null) {
            tabHide.setOnClickListener(v -> {
                hideFloat();
            });
        }

        // 功能页
        TextView tabFeatures = floatView.findViewById(R.id.tabFeatures);
        if (tabFeatures != null) {
            tabFeatures.setOnClickListener(v -> {
                Intent intent = new Intent(this, FeaturesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }

        // 设置页
        TextView tabSettings = floatView.findViewById(R.id.tabSettings);
        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }

        // 主题页
        TextView tabTheme = floatView.findViewById(R.id.tabTheme);
        if (tabTheme != null) {
            tabTheme.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }
    }

    private void hideFloat() {
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
        }
    }
}

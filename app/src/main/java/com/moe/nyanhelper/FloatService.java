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
import android.widget.Switch;
import android.widget.TextView;

public class FloatService extends Service {

    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View floatView;
    private ImageButton float_ball;
    private LinearLayout float_panel;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        showFloat();
    }

    private void showFloat() {
        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);

        float_ball = floatView.findViewById(R.id.float_ball);
        float_panel = floatView.findViewById(R.id.float_panel);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        wm.addView(floatView, params);

        float_ball.setOnClickListener(v -> {
            float_panel.setVisibility(float_panel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // 功能
        TextView tabFeatures = floatView.findViewById(R.id.tabFeatures);
        if (tabFeatures != null) {
            tabFeatures.setOnClickListener(v -> {
                Intent intent = new Intent(this, FeaturesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }

        // 设置
        TextView tabSettings = floatView.findViewById(R.id.tabSettings);
        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }

        // 主题
        TextView tabTheme = floatView.findViewById(R.id.tabTheme);
        if (tabTheme != null) {
            tabTheme.setOnClickListener(v -> {
                Intent intent = new Intent(this, ThemeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                float_panel.setVisibility(View.GONE);
            });
        }

        // 隐藏
        TextView tabHide = floatView.findViewById(R.id.tabHide);
        if (tabHide != null) {
            tabHide.setOnClickListener(v -> {
                if (floatView != null) wm.removeView(floatView);
                floatView = null;
                stopSelf();
            });
        }
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

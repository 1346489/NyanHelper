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
import android.widget.Button;
import android.widget.ImageView;

public class FloatWindowService extends Service {

    private WindowManager wm;
    private View floatView;
    private View panelView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloat();
        return START_STICKY;
    }

    private void showFloat() {
        if (floatView != null) return;

        floatView = LayoutInflater.from(this).inflate(R.layout.float_window, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        ImageView avatar = floatView.findViewById(R.id.floatAvatar);
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                int size = Math.min(view.getWidth(), view.getHeight());
                outline.setOval(0, 0, size, size);
            }
        });

        floatView.setOnClickListener(v -> showPanel());
        wm.addView(floatView, params);
    }

    private void showPanel() {
        if (panelView != null) return;

        panelView = LayoutInflater.from(this).inflate(R.layout.float_panel, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 120;
        params.y = 300;

        Button close = panelView.findViewById(R.id.panelClose);
        close.setOnClickListener(v -> {
            wm.removeView(panelView);
            panelView = null;
        });

        wm.addView(panelView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) wm.removeView(floatView);
        if (panelView != null) wm.removeView(panelView);
    }
}

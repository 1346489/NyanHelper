package com.moe.nyanhelper;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class FloatService extends Service {

    private WindowManager wm;
    private View floatView, panelView;

    private int screenW, screenH;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;

        showFloatBall();
    }

    private void showFloatBall() {
        if (floatView != null) return;

        floatView = LayoutInflater.from(this).inflate(R.layout.float_ball, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        // 默认放右边偏下，不是太靠上
        params.x = screenW - dp(110);
        params.y = screenH / 3;

        floatView.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int startX, startY;
            long downTime;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startX = params.x;
                        startY = params.y;
                        downTime = System.currentTimeMillis();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > dp(6) || Math.abs(dy) > dp(6)) moved = true;

                        params.x = (int) (startX + dx);
                        params.y = (int) (startY + dy);

                        // 限制边界
                        params.x = Math.max(0, Math.min(params.x, screenW - v.getWidth()));
                        params.y = Math.max(0, Math.min(params.y, screenH - v.getHeight()));

                        wm.updateViewLayout(floatView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // 松手贴边
                        if (params.x + v.getWidth() / 2 > screenW / 2) {
                            params.x = screenW - v.getWidth() - dp(8);
                        } else {
                            params.x = dp(8);
                        }
                        wm.updateViewLayout(floatView, params);

                        if (!moved && System.currentTimeMillis() - downTime < 300) {
                            showPanel();
                        }
                        return true;
                }
                return false;
            }
        });

        wm.addView(floatView, params);
    }

    private void showPanel() {
        if (panelView != null) return;

        panelView = LayoutInflater.from(this).inflate(R.layout.float_panel, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dp(150),
                WindowManager.LayoutParams.WRAP_CONTENT,
                type(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;

        // 面板跟悬浮球附近，但别太靠上
        params.x = Math.max(dp(8), Math.min(screenW - dp(160), floatView != null ? getFloatX() + dp(10) : screenW / 2));
        params.y = Math.max(dp(120), Math.min(screenH - dp(360), floatView != null ? getFloatY() + dp(20) : screenH / 3));

        Button btnHide = panelView.findViewById(R.id.panelClose);
        Button btnSettings = panelView.findViewById(R.id.btnSettings);
        Button btnFeatures = panelView.findViewById(R.id.btnFeatures);
        Button btnTheme = panelView.findViewById(R.id.btnTheme);

        btnHide.setOnClickListener(v -> {
            hidePanel();
            hideFloat();
            stopSelf();
        });

        btnSettings.setOnClickListener(v -> {
            startActivityShort(SettingsActivity.class, "设置界面");
            hidePanel();
        });

        btnFeatures.setOnClickListener(v -> {
            startActivityShort(FeaturesActivity.class, "功能界面");
            hidePanel();
        });

        btnTheme.setOnClickListener(v -> {
            startActivityShort(ThemeActivity.class, "主题界面");
            hidePanel();
        });

        wm.addView(panelView, params);
    }

    private void startActivityShort(Class<?> cls, String name) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, name + "待实现/打开失败", Toast.LENGTH_SHORT).show();
        }
    }

    private int getFloatX() {
        if (floatView == null) return screenW - dp(110);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) floatView.getLayoutParams();
        return p.x;
    }

    private int getFloatY() {
        if (floatView == null) return screenH / 3;
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) floatView.getLayoutParams();
        return p.y;
    }

    private void hidePanel() {
        if (panelView != null) {
            wm.removeView(panelView);
            panelView = null;
        }
    }

    private void hideFloat() {
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
        }
    }

    private int type() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePanel();
        hideFloat();
    }
}

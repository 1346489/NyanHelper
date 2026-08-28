package com.moe.nyanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectOverlay {

    private final Context context;
    private final WindowManager wm;
    private final WindowManager.LayoutParams params;
    private final EffectView view;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private boolean snowRunning = false;
    private boolean meteorRunning = false;

    private final List<SnowFlake> snowList = new ArrayList<>();
    private final List<Meteor> meteorList = new ArrayList<>();

    private static final int SNOW_COUNT = 28;

    public EffectOverlay(Context context) {
        this.context = context;
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;

        view = new EffectView(context);
        try {
            wm.addView(view, params);
        } catch (Exception ignored) {
        }
    }

    public void startSnow() {
        snowRunning = true;
        initSnow();
        scheduleFrame();
    }

    public void stopSnow() {
        snowRunning = false;
        snowList.clear();
        view.invalidate();
    }

    public void startMeteor() {
        meteorRunning = true;
        scheduleFrame();
    }

    public void stopMeteor() {
        meteorRunning = false;
        meteorList.clear();
        view.invalidate();
    }

    public void destroy() {
        snowRunning = false;
        meteorRunning = false;
        handler.removeCallbacksAndMessages(null);
        try {
            wm.removeView(view);
        } catch (Exception ignored) {
        }
    }

    private void initSnow() {
        snowList.clear();
        int w = view.getWidth() > 0 ? view.getWidth() : 1080;
        int h = view.getHeight() > 0 ? view.getHeight() : 1920;
        for (int i = 0; i < SNOW_COUNT; i++) {
            snowList.add(new SnowFlake(
                    random.nextFloat() * w * 0.5f,       // 集中在左上区域
                    random.nextFloat() * h,
                    random.nextFloat() * 4 + 2,
                    random.nextFloat() * 2 + 1));
        }
    }

    private void scheduleFrame() {
        handler.postDelayed(() -> {
            if (!snowRunning && !meteorRunning) return;

            int w = view.getWidth();
            int h = view.getHeight();

            if (snowRunning) {
                if (snowList.isEmpty()) initSnow();
                for (SnowFlake s : snowList) {
                    s.y += s.speed;
                    s.x += s.drift;
                    if (s.y > h) {
                        s.y = -10;
                        s.x = random.nextFloat() * w * 0.5f;
                    }
                }
            }

            if (meteorRunning) {
                if (meteorList.isEmpty() || allMeteorsDone()) {
                    spawnMeteor(w, h);
                }
                for (int i = meteorList.size() - 1; i >= 0; i--) {
                    Meteor m = meteorList.get(i);
                    m.progress += 0.02f;
                    if (m.progress >= 1f) meteorList.remove(i);
                }
            }

            view.setSnow(snowList);
            view.setMeteors(meteorList);
            view.invalidate();

            scheduleFrame();
        }, 40); // ~25fps
    }

    private boolean allMeteorsDone() {
        for (Meteor m : meteorList) if (m.progress < 1f) return false;
        return true;
    }

    private void spawnMeteor(int w, int h) {
        // 从右上角出现，向左下坠落
        meteorList.add(new Meteor(
                w * 0.7f + random.nextFloat() * w * 0.3f,  // 右上区域
                -20,
                w * 0.5f,  // 水平位移
                h * 0.6f   // 垂直位移
        ));
    }

    // ===== 绘制 View =====

    private static class EffectView extends View {
        private List<SnowFlake> snow = new ArrayList<>();
        private List<Meteor> meteors = new ArrayList<>();
        private final Paint snowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint meteorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint meteorTail = new Paint(Paint.ANTI_ALIAS_FLAG);

        EffectView(Context c) {
            super(c);
            setBackgroundColor(0x00000000);
            snowPaint.setColor(0xFFFFFFFF);
            meteorPaint.setColor(0xFFFFFFAA);
            meteorTail.setColor(0x88FFFFFF);
            meteorTail.setStrokeWidth(4);
        }

        void setSnow(List<SnowFlake> s) { snow = s; }
        void setMeteors(List<Meteor> m) { meteors = m; }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            for (SnowFlake s : snow) {
                canvas.drawCircle(s.x, s.y, s.radius, snowPaint);
            }

            for (Meteor m : meteors) {
                float t = m.progress;
                float x = m.startX - m.dx * t;
                float y = m.startY + m.dy * t;
                // 尾迹
                canvas.drawLine(x, y, x + m.dx * 0.25f, y - m.dy * 0.25f, meteorTail);
                canvas.drawCircle(x, y, 5, meteorPaint);
            }
        }
    }

    private static class SnowFlake {
        float x, y, speed, drift, radius;
        SnowFlake(float x, float y, float speed, float radius) {
            this.x = x; this.y = y; this.speed = speed; this.radius = radius;
            this.drift = 0.5f;
        }
    }

    private static class Meteor {
        float startX, startY, dx, dy, progress;
        Meteor(float x, float y, float dx, float dy) {
            this.startX = x; this.startY = y; this.dx = dx; this.dy = dy;
            this.progress = 0;
        }
    }
}

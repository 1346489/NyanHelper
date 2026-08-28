package com.moe.nyanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectView extends View {

    public static final int MODE_NONE = 0;
    public static final int MODE_SNOW = 1;
    public static final int MODE_METEOR = 2;

    private int mode = MODE_NONE;
    private final Random random = new Random();
    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final List<Meteor> meteors = new ArrayList<>();

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 动画循环
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (mode == MODE_SNOW) updateSnow();
            else if (mode == MODE_METEOR) updateMeteor();
            invalidate();
            if (mode != MODE_NONE) postDelayed(this, 32); // ~30fps
        }
    };

    public EffectView(Context context) { super(context); }
    public EffectView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setMode(int m) {
        if (m == mode) return;
        mode = m;
        removeCallbacks(tick);
        if (mode == MODE_SNOW) {
            initSnow();
            post(tick);
        } else if (mode == MODE_METEOR) {
            initMeteor();
            post(tick);
        } else {
            snowflakes.clear();
            meteors.clear();
            invalidate();
        }
    }

    // ============ 雪花 ============
    private void initSnow() {
        snowflakes.clear();
        int w = getWidth(), h = getHeight();
        if (w == 0) w = 1080; if (h == 0) h = 1920;
        int count = 60;
        for (int i = 0; i < count; i++) {
            snowflakes.add(new Snowflake(
                    random.nextInt(w),
                    random.nextInt(h),
                    4 + random.nextInt(8),
                    1 + random.nextFloat() * 3,
                    random.nextFloat() * 360));
        }
    }

    private void updateSnow() {
        int h = getHeight();
        for (Snowflake s : snowflakes) {
            s.y += s.speed;
            s.x += (float) Math.sin(Math.toRadians(s.wobble)) * 1.5f;
            s.wobble += 3;
            if (s.y > h + 10) {
                s.y = -10;
                s.x = random.nextInt(getWidth());
            }
        }
    }

    // ============ 流星 ============
    private void initMeteor() {
        meteors.clear();
        spawnMeteor();
    }

    private void spawnMeteor() {
        int w = getWidth(), h = getHeight();
        if (w == 0) w = 1080;
        // 从右上角区域出发，向左下坠落
        meteors.add(new Meteor(
                w * 0.6f + random.nextInt((int) (w * 0.35f)), // 右上区域 x
                -random.nextInt(h / 3),                         // 顶部上方
                8 + random.nextInt(6)                          // 速度
        ));
    }

    private void updateMeteor() {
        int w = getWidth(), h = getHeight();
        for (int i = meteors.size() - 1; i >= 0; i--) {
            Meteor m = meteors.get(i);
            m.x -= m.speed * 1.2f;
            m.y += m.speed;
            if (m.x < -100 || m.y > h + 100) {
                meteors.remove(i);
            }
        }
        // 保持画面里总有流星，但数量少（一个接一个）
        if (meteors.isEmpty() && mode == MODE_METEOR) {
            spawnMeteor();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == MODE_SNOW) {
            paint.setColor(0xFFFFFFFF);
            for (Snowflake s : snowflakes) {
                paint.setAlpha((int) (160 + 95 * Math.sin(Math.toRadians(s.wobble))));
                canvas.drawCircle(s.x, s.y, s.radius, paint);
            }
        } else if (mode == MODE_METEOR) {
            for (Meteor m : meteors) {
                // 流星尾巴（渐变线段）
                paint.setColor(0xFFFFFFFF); paint.setAlpha(230);
                canvas.drawCircle(m.x, m.y, 4, paint);
                paint.setColor(0xFFFFE0A0); paint.setAlpha(140);
                canvas.drawLine(m.x + 18, m.y - 15, m.x, m.y, paint);
                paint.setColor(0xFFFFB060); paint.setAlpha(80);
                canvas.drawLine(m.x + 36, m.y - 30, m.x, m.y, paint);
            }
        }
    }

    // ============ 数据类 ============
    private static class Snowflake {
        float x, y;
        float radius;
        float speed;
        float wobble;
        Snowflake(float x, float y, float r, float s, float w) {
            this.x = x; this.y = y; this.radius = r; this.speed = s; this.wobble = w;
        }
    }

    private static class Meteor {
        float x, y;
        float speed;
        Meteor(float x, float y, float s) { this.x = x; this.y = y; this.speed = s; }
    }
}

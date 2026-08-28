package com.moe.nyanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 悬浮球上方的动态特效层：
 * - 雪花：在悬浮球范围内、左上角区域持续飘落（动态）
 * - 流星：从右上角出现，斜向下坠落（动态）
 * 两者互斥，只有一个会运行
 */
public class ViewEffectOverlay extends View {

    private final Random random = new Random();
    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final List<Meteor> meteors = new ArrayList<>();

    private boolean snowing = false;
    private boolean meteorShower = false;

    private final Paint snowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint meteorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 动画帧间隔
    private static final int SNOW_COUNT = 30;
    private static final long FRAME_DELAY = 50; // ~20fps

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!snowing && !meteorShower) return;
            update();
            invalidate();
            if (snowing || meteorShower) {
                postDelayed(this, FRAME_DELAY);
            }
        }
    };

    public ViewEffectOverlay(Context context) {
        super(context);
        init();
    }
    public ViewEffectOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ViewEffectOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        snowPaint.setColor(0xFFFFFFFF);
        snowPaint.setStyle(Paint.Style.FILL);
        meteorPaint.setStyle(Paint.Style.STROKE);
        meteorPaint.setStrokeCap(Paint.Cap.ROUND);
        setWillNotDraw(false);
    }

    /* ==================== 控制接口 ==================== */

    public void startSnow() {
        stopAll(); // 互斥：先停掉流星
        snowing = true;
        snowflakes.clear();
        for (int i = 0; i < SNOW_COUNT; i++) {
            snowflakes.add(new Snowflake());
        }
        post(tickRunnable);
        invalidate();
    }

    public void startMeteor() {
        stopAll(); // 互斥：先停掉雪花
        meteorShower = true;
        meteors.clear();
        post(tickRunnable);
        invalidate();
    }

    public void stopAll() {
        snowing = false;
        meteorShower = false;
        snowflakes.clear();
        meteors.clear();
        removeCallbacks(tickRunnable);
        invalidate();
    }

    /* ==================== 帧更新 ==================== */

    private void update() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (snowing) {
            for (Snowflake s : snowflakes) {
                s.y += s.speed;
                s.x += s.drift;
                if (s.y > h) {
                    s.y = -s.radius;
                    s.x = random.nextFloat() * w * 0.6f; // 偏左上角区域
                }
            }
        }

        if (meteorShower) {
            // 定期生成流星
            if (meteors.isEmpty() || (meteors.size() < 3 && random.nextFloat() < 0.08f)) {
                meteors.add(new Meteor(w, h));
            }
            for (int i = meteors.size() - 1; i >= 0; i--) {
                Meteor m = meteors.get(i);
                m.progress += 0.04f;
                if (m.progress >= 1f) {
                    meteors.remove(i);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        if (snowing) {
            for (Snowflake s : snowflakes) {
                snowPaint.setAlpha((int) (180 + random.nextInt(75)));
                canvas.drawCircle(s.x, s.y, s.radius, snowPaint);
            }
        }

        if (meteorShower) {
            for (Meteor m : meteors) {
                float p = m.progress;
                float startX = m.startX, startY = m.startY;
                float endX = m.endX, endY = m.endY;
                float cx = startX + (endX - startX) * p;
                float cy = startY + (endY - startY) * p;
                // 尾迹长度
                float tail = 0.25f;
                float tx = startX + (endX - startX) * Math.max(0, p - tail);
                float ty = startY + (endY - startY) * Math.max(0, p - tail);
                float alpha = p < 0.8f ? 255 : (int) (255 * (1 - (p - 0.8f) / 0.2f));
                meteorPaint.setAlpha(Math.max(0, alpha));
                meteorPaint.setStrokeWidth(4f);
                canvas.drawLine(tx, ty, cx, cy, meteorPaint);
                // 流星头亮点
                Paint head = new Paint(Paint.ANTI_ALIAS_FLAG);
                head.setColor(0xFFFFFFFF);
                head.setAlpha(Math.max(0, alpha));
                canvas.drawCircle(cx, cy, 3f, head);
            }
        }
    }

    /* ==================== 内部类 ==================== */

    private class Snowflake {
        float x, y, radius, speed, drift;
        Snowflake() {
            radius = 3f + random.nextFloat() * 5f;
            speed = 1.5f + random.nextFloat() * 3f;
            drift = -0.5f + random.nextFloat() * 1f;
            x = random.nextFloat() * 1000f;
            y = random.nextFloat() * 600f;
        }
    }

    private class Meteor {
        float startX, startY, endX, endY, progress;
        Meteor(int w, int h) {
            startX = w * (0.7f + random.nextFloat() * 0.3f); // 右上角
            startY = -20f;
            endX = startX - w * (0.4f + random.nextFloat() * 0.3f);
            endY = h * (0.7f + random.nextFloat() * 0.3f);
            progress = 0f;
        }
    }
}

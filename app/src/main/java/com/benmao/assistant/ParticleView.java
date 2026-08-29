package com.benmao.assistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleView extends View {

    private static final int MAX = 60;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int mode = -1; // -1 无, 0 雪花, 1 流星雨
    private int width, height;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ParticleView(Context context) { super(context); init(); }
    public ParticleView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        setBackgroundColor(0x00000000);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w; height = h;
        if (mode >= 0) setMode(mode); // 尺寸确定后重建粒子，避免越界
    }

    public void setMode(int mode) {
        this.mode = mode;
        particles.clear();
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (mode == 0) {
            for (int i = 0; i < MAX; i++) {
                particles.add(new Particle(random.nextInt(w), random.nextInt(h),
                        4 + random.nextInt(6), 2 + random.nextFloat() * 4));
            }
        } else if (mode == 1) {
            for (int i = 0; i < 20; i++) {
                particles.add(new Particle(random.nextInt(w), random.nextInt(Math.max(1, h / 2)),
                        2 + random.nextInt(4), 8 + random.nextFloat() * 10));
            }
        }
        handler.removeCallbacks(frame);
        if (mode >= 0) handler.post(frame); else invalidate();
    }

    private final Runnable frame = new Runnable() {
        @Override
        public void run() {
            if (mode < 0) return;
            for (Particle p : particles) {
                if (mode == 0) { // 雪花：缓慢下落
                    p.y += p.speed;
                    p.x += Math.sin(p.y / 30f) * 1.5f;
                    if (p.y > height) { p.y = -10; p.x = random.nextInt(width); }
                } else { // 流星：快速斜下
                    p.y += p.speed;
                    p.x -= p.speed * 0.6f;
                    if (p.y > height || p.x < 0) {
                        p.y = random.nextInt(height / 2);
                        p.x = width - random.nextInt(width / 3);
                    }
                }
            }
            invalidate();
            handler.postDelayed(this, 33); // ~30fps
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode < 0 || width == 0) return;
        for (Particle p : particles) {
            if (mode == 0) {
                paint.setColor(0xFFFFFFFF);
                canvas.drawCircle(p.x, p.y, p.size, paint);
            } else {
                paint.setColor(0xFFFFFFAA);
                canvas.drawLine(p.x, p.y, p.x + 12, p.y + 18, paint);
            }
        }
    }

    private static class Particle {
        float x, y;
        float size;
        float speed;
        Particle(float x, float y, float size, float speed) {
            this.x = x; this.y = y; this.size = size; this.speed = speed;
        }
    }
}

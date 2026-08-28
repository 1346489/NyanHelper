package com.moe.nyanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 悬浮球内的特效层：雪花（左上区域）+ 流星（右上斜落）。
 * 继承 SurfaceView，自己在子线程绘制，避免阻塞主线程。
 */
public class SnowMeteorView extends SurfaceView implements SurfaceHolder.Callback {

    private final Paint paint;
    private final List<float[]> snow = new ArrayList<>();
    private final List<float[]> meteors = new ArrayList<>();
    private final Random rand = new Random();

    private boolean running;
    private Thread thread;
    private boolean snowOn;
    private boolean meteorOn;
    private int w, h;

    public SnowMeteorView(Context context) {
        super(context);
        // 关键：通过 getHolder() 获取 SurfaceHolder，不把 int 当 SurfaceHolder 用
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        refreshConfig(NyanConfig.isSnow(context), NyanConfig.isMeteor(context));
    }

    /** 设置页开关变化时调用，刷新特效开关状态 */
    public void refreshConfig(boolean snow, boolean meteor) {
        this.snowOn = snow;
        this.meteorOn = meteor;
        if (!snowOn) snow.clear();
        if (!meteorOn) meteors.clear();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        w = getWidth();
        h = getHeight();
        ensureParticles();
        running = true;
        thread = new Thread(this::runDraw);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        w = width;
        h = height;
        ensureParticles();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        if (thread != null) {
            try { thread.join(500); } catch (InterruptedException ignored) {}
        }
        snow.clear();
        meteors.clear();
    }

    /** 补齐粒子（数量控制，避免太多卡顿）*/
    private void ensureParticles() {
        if (w <= 0 || h <= 0) return;

        if (snowOn && snow.size() < 30) {
            for (int i = snow.size(); i < 30; i++) {
                snow.add(new float[]{
                        rand.nextFloat() * w,          // x
                        rand.nextFloat() * h,          // y
                        1f + rand.nextFloat() * 2f,    // 下落速度
                        120 + rand.nextInt(80)         // 透明度
                });
            }
        }

        if (meteorOn && meteors.size() < 2) {
            for (int i = meteors.size(); i < 2; i++) {
                meteors.add(new float[]{
                        rand.nextFloat() * w,          // x
                        rand.nextFloat() * h * 0.5f,   // y（上半区）
                        6f + rand.nextFloat() * 4f,    // 水平速度
                        180 + rand.nextInt(60),        // 透明度
                        200 + rand.nextInt(55)         // 生命周期
                });
            }
        }
    }

    private void runDraw() {
        while (running) {
            // 正确用法：holder 是 SurfaceHolder（来自 getHolder()），不是 int
            SurfaceHolder holder = getHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas == null) continue;
                synchronized (holder) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (snowOn) drawSnow(canvas);
                    if (meteorOn) drawMeteor(canvas);
                }
            } finally {
                if (canvas != null) {
                    try { holder.unlockCanvasAndPost(canvas); } catch (Exception ignored) {}
                }
            }
            try { Thread.sleep(30); } catch (InterruptedException e) { running = false; }
        }
    }

    private void drawSnow(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (float[] p : snow) {
            paint.setAlpha((int) p[3]);
            canvas.drawCircle(p[0], p[1], 2f + p[2] * 0.3f, paint);
            p[1] += p[2];       // 下落
            p[0] += 0.3f;       // 微风飘
            if (p[1] > h) {
                p[1] = -10;
                p[0] = rand.nextFloat() * Math.max(w, 1);
            }
        }
    }

    private void drawMeteor(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (float[] m : meteors) {
            paint.setAlpha((int) m[3]);
            // 尾迹
            canvas.drawLine(m[0], m[1], m[0] - 18, m[1] + 8, paint);
            // 头部亮点
            canvas.drawCircle(m[0], m[1], 2f, paint);
            m[0] += m[2];
            m[1] += m[2] * 0.45f;
            m[3] -= 4;          // 逐渐消失
            if (m[0] > w || m[1] > h || m[3] <= 0) {
                m[0] = rand.nextFloat() * Math.max(w, 1) * 0.8f;
                m[1] = -20;
                m[3] = 180 + rand.nextInt(60);
            }
        }
    }
}

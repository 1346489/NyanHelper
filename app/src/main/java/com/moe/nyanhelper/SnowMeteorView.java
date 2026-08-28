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
 * 雪花 + 流星 特效视图。
 * - 雪花：白色圆点，从顶部缓慢下落 + 左右飘，限定在左上区域
 * - 流星：从右上向左下坠落，带尾迹 + 头部亮点
 * 通过 refreshConfig(snow, meteor) 开关，由 FloatService 在设置变化时调用。
 */
public class SnowMeteorView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final SurfaceHolder holder;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rnd = new Random();
    private Thread thread;
    private boolean running;

    private boolean snow, meteor;
    private final List<float[]> snows = new ArrayList<>();
    private final List<float[]> meteors = new ArrayList<>();
    private int w, h;

    public SnowMeteorView(Context context) {
        super(context);
        setZOrderOnTop(true);
        holder = getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
        holder.addCallback(this);
    }

    /** 设置开关；线程安全（surface 未创建时也能缓存配置） */
    public void refreshConfig(boolean snow, boolean meteor) {
        this.snow = snow;
        this.meteor = meteor;
        if (snow && w > 0) initSnow();
        else snows.clear();
        if (meteor && w > 0) initMeteor();
        else meteors.clear();
    }

    private void initSnow() {
        snows.clear();
        int count = Math.max(20, w * h / 12000);
        for (int i = 0; i < count; i++) {
            snows.add(new float[]{
                    rnd.nextFloat() * w,           // x（左上区域）
                    rnd.nextFloat() * h,
                    2 + rnd.nextFloat() * 4,        // 半径
                    0.6f + rnd.nextFloat() * 1.6f,  // 下落速度
                    120 + rnd.nextInt(120)          // 透明度
            });
        }
    }

    private void initMeteor() {
        meteors.clear();
        for (int i = 0; i < 4; i++) {
            meteors.add(new float[]{
                    rnd.nextFloat() * w * 0.7f,     // x（右上区域起点）
                    rnd.nextFloat() * h * 0.4f,
                    4 + rnd.nextFloat() * 4,        // 速度 x
                    2 + rnd.nextFloat() * 3,        // 速度 y
                    140 + rnd.nextInt(110)          // 透明度
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder h) {
        w = getWidth();
        h = getHeight();
        refreshConfig(snow, meteor);
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
        w = width;
        h = height;
        refreshConfig(snow, meteor);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder h) {
        running = false;
        if (thread != null) {
            try { thread.join(500); } catch (InterruptedException ignored) {}
            thread = null;
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas == null) continue;
                synchronized (holder) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (snow)   drawSnow(canvas);
                    if (meteor) drawMeteor(canvas);
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
            try { Thread.sleep(30); } catch (InterruptedException e) { break; }
        }
    }

    private void drawSnow(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (float[] p : snows) {
            p[0] += Math.sin(p[1] / 30f) * 0.5f;  // 左右飘
            p[1] += p[3];                           // 下落
            if (p[1] > h) { p[1] = -10; p[0] = rnd.nextFloat() * w * 0.6f; }
            paint.setAlpha((int) p[4]);
            canvas.drawCircle(p[0], p[1], p[2], paint);
        }
    }

    private void drawMeteor(Canvas canvas) {
        paint.setColor(Color.parseColor("#FFE6F2"));
        for (float[] m : meteors) {
            m[0] += m[2];   // 向右下（右上起点 → 实际向右下）
            m[1] += m[3];
            if (m[0] > w || m[1] > h) {
                m[0] = rnd.nextFloat() * w * 0.5f - 60;
                m[1] = rnd.nextFloat() * h * 0.3f;
            }
            paint.setAlpha((int) m[4]);
            canvas.drawLine(m[0], m[1], m[0] - 20, m[1] + 10, paint); // 尾迹
            canvas.drawCircle(m[0], m[1], 2.5f, paint);              // 头部亮点
        }
    }
}

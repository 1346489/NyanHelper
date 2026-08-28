package com.moe.nyanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowMeteorView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rand = new Random();

    private boolean running;
    private boolean snowOn;
    private boolean meteorOn;

    private final List<Snow> snow = new ArrayList<>();
    private final List<Meteor> meteors = new ArrayList<>();

    public SnowMeteorView(Context context) {
        super(context);
        init();
    }

    public SnowMeteorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnowMeteorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.WHITE);
    }

    public void refreshConfig(boolean snowEnabled, boolean meteorEnabled) {
        this.snowOn = snowEnabled;
        this.meteorOn = meteorEnabled;

        if (!snowOn) {
            snow.clear();
        }
        if (!meteorOn) {
            meteors.clear();
        }

        if (snowOn || meteorOn) {
            startEffect();
        } else {
            stopEffect();
        }
    }

    public void stopEffect() {
        running = false;
        snow.clear();
        meteors.clear();
        invalidate();
    }

    private void startEffect() {
        if (!running) {
            running = true;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (snowOn) {
            updateAndDrawSnow(canvas);
        }
        if (meteorOn) {
            updateAndDrawMeteors(canvas);
        }

        if (running && (snowOn || meteorOn)) {
            postInvalidateDelayed(16);
        }
    }

    private void updateAndDrawSnow(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        while (snow.size() < 40) {
            snow.add(new Snow(rand.nextFloat() * w, rand.nextFloat() * h));
        }

        paint.setColor(Color.WHITE);
        for (Snow s : snow) {
            s.y += s.speed;
            s.x += Math.sin(s.y / 40f) * 0.6f;
            if (s.y > h) {
                s.y = -10;
                s.x = rand.nextFloat() * w;
            }
            canvas.drawCircle(s.x, s.y, s.r, paint);
        }
    }

    private void updateAndDrawMeteors(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        while (meteors.size() < 3) {
            meteors.add(new Meteor(rand.nextFloat() * w, rand.nextFloat() * h * 0.5f));
        }

        paint.setColor(Color.argb(220, 255, 220, 255));
        for (Meteor m : meteors) {
            m.x -= m.speed;
            m.y += m.speed * 0.4f;
            if (m.x < -100 || m.y > h + 100) {
                m.x = w + rand.nextFloat() * w;
                m.y = rand.nextFloat() * h * 0.5f;
            }
            canvas.drawLine(m.x, m.y, m.x + 18, m.y - 8, paint);
        }
    }

    static class Snow {
        float x, y, speed, r;

        Snow(float x, float y) {
            this.x = x;
            this.y = y;
            Random r = new Random();
            this.speed = 1f + r.nextFloat() * 2f;
            this.r = 1.5f + r.nextFloat() * 2.5f;
        }
    }

    static class Meteor {
        float x, y, speed;

        Meteor(float x, float y) {
            this.x = x;
            this.y = y;
            this.speed = 6f + new Random().nextFloat() * 4f;
        }
    }
}

package com.benmao.assistant;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Handles touch events for the floating ball:
 * - Drag to move around screen
 * - Tap to open menu (returns false on tap so onClick fires)
 */
public class BallTouchListener implements View.OnTouchListener {

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isMoved = false;
    private static final int CLICK_THRESHOLD = 8;

    public BallTouchListener(WindowManager wm, WindowManager.LayoutParams p) {
        this.windowManager = wm;
        this.params = p;
    }

    public void updateParams(WindowManager.LayoutParams p) {
        this.params = p;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (windowManager == null || params == null) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                isMoved = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - initialTouchX;
                float deltaY = event.getRawY() - initialTouchY;

                if (Math.abs(deltaX) > CLICK_THRESHOLD || Math.abs(deltaY) > CLICK_THRESHOLD) {
                    isMoved = true;
                }

                params.x = (int) (initialX + deltaX);
                params.y = (int) (initialY + deltaY);
                try {
                    windowManager.updateViewLayout(v, params);
                } catch (Exception ignored) {}
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // If not moved much, it's a tap - let onClick handle it
                return isMoved; // Return true if moved (consume), false if tap (let onClick fire)

            default:
                return false;
        }
    }
}

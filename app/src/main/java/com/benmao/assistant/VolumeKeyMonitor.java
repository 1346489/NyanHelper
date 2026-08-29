package com.benmao.assistant;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class VolumeKeyMonitor {

    public static boolean dispatch(Context context, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        // 仅在悬浮窗运行中生效（通过简单的偏好判断）
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            Intent i = new Intent(OverlayWindowService.ACTION_VOLUME_UP);
            context.sendBroadcast(i);
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Intent i = new Intent(OverlayWindowService.ACTION_VOLUME_DOWN);
            context.sendBroadcast(i);
            return true;
        }
        return false;
    }
}

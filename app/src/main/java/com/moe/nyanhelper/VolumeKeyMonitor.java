package com.benmao.assistant;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

/**
 * Monitors volume key presses using AudioManager and a polling approach.
 * When volume changes are detected, we infer volume up/down key presses.
 */
public class VolumeKeyMonitor {

    public interface VolumeKeyListener {
        void onVolumeUp();
        void onVolumeDown();
    }

    private Context context;
    private AudioManager audioManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private VolumeKeyListener listener;
    private boolean isRunning = false;

    private int lastVolume = -1;
    private static final int CHECK_INTERVAL = 200; // ms

    private Runnable volumeCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            int currentVolume = getCurrentVolume();
            if (lastVolume >= 0 && currentVolume != lastVolume) {
                if (currentVolume > lastVolume) {
                    if (listener != null) listener.onVolumeUp();
                } else {
                    if (listener != null) listener.onVolumeDown();
                }
            }
            lastVolume = currentVolume;
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    public VolumeKeyMonitor(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void start(VolumeKeyListener listener) {
        this.listener = listener;
        this.isRunning = true;
        this.lastVolume = getCurrentVolume();
        handler.post(volumeCheckRunnable);
    }

    public void stop() {
        this.isRunning = false;
        handler.removeCallbacks(volumeCheckRunnable);
    }

    private int getCurrentVolume() {
        if (audioManager == null) return 0;
        try {
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            return 0;
        }
    }
}

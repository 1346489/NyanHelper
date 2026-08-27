package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 文字替换逻辑后续加这里
    }

    @Override
    public void onInterrupt() {
    }
}

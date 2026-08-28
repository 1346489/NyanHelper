package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                CharSequence text = source.getText();
                if (text != null) {
                    String orig = text.toString();
                    String replaced = NyanConfig.apply(this, orig);
                    if (!orig.equals(replaced)) {
                        source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT);
                        // 通过剪贴板或直接设置
                        android.os.Bundle args = new android.os.Bundle();
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replaced);
                        source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {}
}

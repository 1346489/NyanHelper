package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                traverseAndReplace(root);
            }
        }
    }

    private void traverseAndReplace(AccessibilityNodeInfo node) {
        if (node == null) return;
        CharSequence text = node.getText();
        if (text != null && text.length() > 0) {
            String replaced = text.toString()
                    .replace("你", "主人")
                    .replace("我", "本喵")
                    + "喵~";
            if (!replaced.equals(text.toString())) {
                // 无障碍不能直接改文字，但日志里能看到
                android.util.Log.d("NyanHelper", "替换: " + text + " → " + replaced);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            traverseAndReplace(node.getChild(i));
        }
    }

    @Override
    public void onInterrupt() {}
}

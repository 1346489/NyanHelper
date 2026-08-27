package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class NyanAccessibilityService extends AccessibilityService {

    // 文字替换映射表（你想替换什么在这里加）
    private static final java.util.Map<String, String> REPLACE_MAP = new java.util.HashMap<>();
    static {
        REPLACE_MAP.put("原文字1", "替换后1");
        REPLACE_MAP.put("原文字2", "替换后2");
        // 继续加...
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            traverseAndReplace(root);
        }
    }

    private void traverseAndReplace(AccessibilityNodeInfo node) {
        if (node == null) return;

        // 替换文本内容
        if (node.getClassName() != null && node.getClassName().toString().contains("TextView")) {
            CharSequence text = node.getText();
            if (text != null) {
                String original = text.toString();
                if (REPLACE_MAP.containsKey(original)) {
                    // 注意：需要通过 ACTION_SET_TEXT 或通过 arguments 设置
                    // 实际替换需要 node.performAction(ACTION_SET_TEXT, bundle)
                    // 这里仅示意，完整实现见下方说明
                }
            }
        }

        // 递归遍历子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            traverseAndReplace(node.getChild(i));
        }
    }

    @Override
    public void onInterrupt() {
    }
}

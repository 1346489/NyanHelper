package com.benmao.assistant;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import android.text.TextUtils;

public class BenmaoAccessibilityService extends AccessibilityService {

    private Prefs prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Prefs(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        // 监听文本变化 / 内容变化
        if (type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
                type != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        try {
            processNode(root);
        } catch (Exception ignored) {
        }
    }

    private void processNode(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.isEditable()) {
            CharSequence text = node.getText();
            if (text != null && text.length() > 0) {
                String processed = transform(text.toString());
                if (!processed.equals(text.toString())) {
                    Bundle args = new Bundle();
                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, processed);
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                }
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            processNode(node.getChild(i));
        }
    }

    String transform(String input) {
        String s = input;
        if (prefs.isReplaceMe()) s = s.replace("我", "本喵");
        if (prefs.isReplaceYou()) s = s.replace("你", "主人");
        if (prefs.isAddMeow() && !s.endsWith("喵")) s = s + "喵";
        return s;
    }

    @Override
    public void onInterrupt() {}
}

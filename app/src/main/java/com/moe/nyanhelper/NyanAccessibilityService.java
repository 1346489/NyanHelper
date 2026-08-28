package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 无障碍服务：监听输入框文本变化，按 NyanConfig 规则替换（你→主人/我→本喵/加喵）。
 * 全局生效，不限聊天软件。
 */
public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return;
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        CharSequence text = source.getText();
        if (text == null) return;

        String orig = text.toString();
        String replaced = NyanConfig.apply(this, orig);
        if (orig.equals(replaced)) return;

        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replaced);
        source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    @Override
    public void onInterrupt() {}
}

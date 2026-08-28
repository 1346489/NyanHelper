package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 无障碍服务：监听输入框文本变化，通过 NyanConfig.apply(this, text) 做替换。
 * 不限聊天软件——只要是可编辑 EditText 都生效。
 */
public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return;

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        // 只处理可编辑的输入框
        if (!source.isEditable()) return;

        CharSequence text = source.getText();
        if (text == null) return;

        String orig = text.toString();
        // 关键：apply 签名是 (Context, String)，第一个参数传 this（AccessibilityService 是 Context）
        String replaced = NyanConfig.apply(this, orig);

        if (orig.equals(replaced)) return;

        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replaced);
        source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    @Override
    public void onInterrupt() {}
}

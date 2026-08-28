package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && type != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        collect(root, nodes);

        for (AccessibilityNodeInfo node : nodes) {
            if (node == null) continue;
            if (!node.isEditable()) continue;

            CharSequence text = node.getText();
            if (text == null) continue;
            String orig = text.toString();
            if (orig.isEmpty()) continue;

            String replaced = NyanConfig.apply(orig);
            if (replaced.equals(orig)) continue;

            try {
                android.os.Bundle args = new android.os.Bundle();
                args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        replaced);
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
            } catch (Exception ignored) {
            }
        }
    }

    private void collect(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> out) {
        if (node == null) return;
        try {
            out.add(AccessibilityNodeInfo.obtain(node));
            for (int i = 0; i < node.getChildCount(); i++) {
                collect(node.getChild(i), out);
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onInterrupt() {
    }
}

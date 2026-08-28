package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NyanAccessibilityService extends AccessibilityService {

    private final Set<Integer> seen = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int t = event.getEventType();
        if (t != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && t != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        try {
            List<AccessibilityNodeInfo> list = new ArrayList<>();
            collect(root, list);
            for (AccessibilityNodeInfo node : list) {
                if (node == null) continue;
                try {
                    CharSequence text = node.getText();
                    if (text == null) continue;
                    String orig = text.toString();
                    int hash = node.hashCode() ^ orig.hashCode();
                    if (seen.contains(hash)) continue;
                    String repl = NyanConfig.apply(orig, this);
                    if (!repl.equals(orig)) {
                        Bundle args = new Bundle();
                        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, repl);
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                        seen.add(hash);
                    }
                } catch (IllegalStateException ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private void collect(AccessibilityNodeInfo root, List<AccessibilityNodeInfo> out) {
        if (root == null) return;
        try {
            if (root.isEditable() && root.getText() != null) out.add(AccessibilityNodeInfo.obtain(root));
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo c = root.getChild(i);
                if (c != null) collect(c, out);
            }
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() { super.onServiceConnected(); seen.clear(); }

    @Override
    public void onDestroy() { super.onDestroy(); seen.clear(); }
}

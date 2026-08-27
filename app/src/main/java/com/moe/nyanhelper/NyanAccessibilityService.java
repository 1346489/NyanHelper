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

    private final Set<Integer> processedNodes = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                && type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        try {
            List<AccessibilityNodeInfo> editableNodes = new ArrayList<>();
            findEditableNodes(root, editableNodes);

            for (AccessibilityNodeInfo node : editableNodes) {
                if (node == null) continue;
                try {
                    CharSequence text = node.getText();
                    if (text == null) continue;

                    String original = text.toString();
                    int nodeHash = node.hashCode() ^ original.hashCode();
                    if (processedNodes.contains(nodeHash)) continue;

                    String replaced = NyanConfig.apply(original, this);
                    if (!replaced.equals(original)) {
                        Bundle args = new Bundle();
                        args.putCharSequence(
                                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                replaced);
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                        processedNodes.add(nodeHash);
                    }
                } catch (IllegalStateException ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void findEditableNodes(AccessibilityNodeInfo root, List<AccessibilityNodeInfo> out) {
        if (root == null) return;
        try {
            if (root.isEditable() && root.getText() != null) {
                out.add(AccessibilityNodeInfo.obtain(root));
            }
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = root.getChild(i);
                if (child != null) {
                    findEditableNodes(child, out);
                }
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        processedNodes.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        processedNodes.clear();
    }
}

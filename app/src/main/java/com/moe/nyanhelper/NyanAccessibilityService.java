package com.moe.nyanhelper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class NyanAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        traverse(root);
    }

    private void traverse(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        try {
            CharSequence text = node.getText();
            if (text != null && node.isEditable()) {
                String original = text.toString();
                String transformed = NyanText.transform(
                        original,
                        Prefs.addMeowTail(this),
                        Prefs.replaceMe(this),
                        Prefs.replaceYou(this)
                );
                if (!transformed.equals(original)) {
                    Bundle args = new Bundle();
                    args.putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            transformed
                    );
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                traverse(node.getChild(i));
            }
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Override
    public void onInterrupt() {
        // ignore
    }
}

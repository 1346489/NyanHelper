package com.benmao.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class BenmaoAccessibilityService extends AccessibilityService {

    public static final String ACTION_UPDATE_SETTINGS = "com.benmao.assistant.UPDATE_SETTINGS";

    private static BenmaoAccessibilityService instance;
    private SharedPreferencesHelper prefs;

    private BroadcastReceiver settingsReceiver;

    public static BenmaoAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = new SharedPreferencesHelper(this);

        // Register settings update receiver
        settingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                prefs.reload();
            }
        };
        registerReceiver(settingsReceiver, new IntentFilter(ACTION_UPDATE_SETTINGS));
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                processTextNodes(rootNode);
            }
        }
    }

    private void processTextNodes(AccessibilityNodeInfo node) {
        if (node == null) return;

        // Check if this is an editable text field
        if (node.isEditable() && node.getText() != null) {
            String text = node.getText().toString();
            String modified = applyTextTransformations(text);

            if (!modified.equals(text)) {
                setNodeText(node, modified);
            }
        }

        // Recurse children
        for (int i = 0; i < node.getChildCount(); i++) {
            processTextNodes(node.getChild(i));
        }
    }

    private String applyTextTransformations(String text) {
        if (TextUtils.isEmpty(text)) return text;

        String result = text;

        // Feature 1: Add "喵" at the end (always)
        if (prefs.isFuncAddMiao()) {
            if (!result.endsWith("喵")) {
                result = result + "喵";
            }
        }

        // Feature 2: Replace "我" with "本喵"
        if (prefs.isFuncReplaceMe()) {
            result = result.replace("我", "本喵");
        }

        // Feature 3: Replace "你" with "主人"
        if (prefs.isFuncReplaceYou()) {
            result = result.replace("你", "主人");
        }

        return result;
    }

    private void setNodeText(AccessibilityNodeInfo node, String text) {
        if (node == null || TextUtils.isEmpty(text)) return;

        Bundle arguments = new Bundle();
        arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    @Override
    public void onInterrupt() {
        // Service interrupted
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (settingsReceiver != null) {
            unregisterReceiver(settingsReceiver);
        }
    }
}

package com.moe.nyanhelper;

public class NyanText {
    public static String process(String text) {
        if (text == null) return null;
        if (Prefs.isFloatOn(null)) { // 简化，实际从 AccessibilityService 传 Context
            text = text.replace("我", "本喵");
            text = text.replace("你", "主人");
            if (!text.endsWith("喵")) text += "喵";
        }
        return text;
    }
}

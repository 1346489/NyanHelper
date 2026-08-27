package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREF_NAME = "nyan_config";
    private static final String KEY_ADD_NYA = "add_nya";
    private static final String KEY_ME = "replace_me";
    private static final String KEY_YOU = "replace_you";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAddNya(Context c) {
        return prefs(c).getBoolean(KEY_ADD_NYA, false);
    }

    public static void setAddNya(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_ADD_NYA, v).apply();
    }

    public static boolean isMe(Context c) {
        return prefs(c).getBoolean(KEY_ME, false);
    }

    public static void setMe(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_ME, v).apply();
    }

    public static boolean isYou(Context c) {
        return prefs(c).getBoolean(KEY_YOU, false);
    }

    public static void setYou(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_YOU, v).apply();
    }

    public static String apply(String text, Context c) {
        if (text == null || text.trim().isEmpty()) return text;
        String s = text;

        // 顺序：先"你"再"我"，避免交叉污染
        if (isYou(c)) {
            s = s.replace("你", "主人");
        }
        if (isMe(c)) {
            s = s.replace("我", "本喵");
        }
        if (isAddNya(c)) {
            s = addNya(s);
        }
        return s;
    }

    private static String addNya(String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        if (s.endsWith("喵") || s.endsWith("喵~") || s.endsWith("喵！") || s.endsWith("喵~！")) {
            return s;
        }
        char last = s.charAt(s.length() - 1);
        if (isPunctuation(last)) {
            return s.substring(0, s.length() - 1) + "喵" + last;
        }
        return s + "喵~";
    }

    private static boolean isPunctuation(char c) {
        return c == '。' || c == '！' || c == '？' || c == '…'
                || c == '.' || c == '!' || c == '?' || c == '~';
    }
}

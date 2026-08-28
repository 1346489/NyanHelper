package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    // ===== 主题常量（ThemeActivity 用）=====
    public static final int THEME_SAKURA = 0;
    public static final int THEME_MINT = 1;
    public static final int THEME_STARRY = 2;

    private static final String PREFS = "nyan_helper_prefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_ADD_NYA = "add_nya";
    private static final String KEY_YOU_TO_MASTER = "you_to_master";
    private static final String KEY_I_TO_ME = "i_to_me";
    private static final String KEY_REPLACE_YOU = "replace_you";
    private static final String KEY_REPLACE_ME = "replace_me";
    private static final String KEY_SNOW = "snow";
    private static final String KEY_METEOR = "meteor";
    private static final String KEY_SERVICE_RUNNING = "service_running";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ===== 主题 =====
    public static int getTheme(Context c) {
        return prefs(c).getInt(KEY_THEME, THEME_SAKURA);
    }
    public static void setTheme(Context c, int theme) {
        prefs(c).edit().putInt(KEY_THEME, theme).apply();
    }

    // ===== 功能开关 =====
    public static boolean isAddNya(Context c) {
        return prefs(c).getBoolean(KEY_ADD_NYA, true);
    }
    public static void setAddNya(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_ADD_NYA, v).apply();
    }

    // "你→主人"：isYouToMaster（FeaturesActivity 用）+ 兼容旧名 isReplaceYou（FloatService/旧代码）
    public static boolean isYouToMaster(Context c) {
        return prefs(c).getBoolean(KEY_YOU_TO_MASTER, true)
                || prefs(c).getBoolean(KEY_REPLACE_YOU, false);
    }
    public static void setYouToMaster(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_YOU_TO_MASTER, v).putBoolean(KEY_REPLACE_YOU, v).apply();
    }
    public static boolean isReplaceYou(Context c) { return isYouToMaster(c); }
    public static void setReplaceYou(Context c, boolean v) { setYouToMaster(c, v); }

    // "我→本喵"：isIToMe + 兼容旧名 isReplaceMe
    public static boolean isIToMe(Context c) {
        return prefs(c).getBoolean(KEY_I_TO_ME, true)
                || prefs(c).getBoolean(KEY_REPLACE_ME, false);
    }
    public static void setIToMe(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_I_TO_ME, v).putBoolean(KEY_REPLACE_ME, v).apply();
    }
    public static boolean isReplaceMe(Context c) { return isIToMe(c); }
    public static void setReplaceMe(Context c, boolean v) { setIToMe(c, v); }

    // ===== 特效开关（互斥由调用方保证，或这里兜底）=====
    public static boolean isSnow(Context c) {
        return prefs(c).getBoolean(KEY_SNOW, false);
    }
    public static void setSnow(Context c, boolean v) {
        SharedPreferences.Editor e = prefs(c).edit();
        e.putBoolean(KEY_SNOW, v);
        if (v) e.putBoolean(KEY_METEOR, false);
        e.apply();
    }
    public static boolean isMeteor(Context c) {
        return prefs(c).getBoolean(KEY_METEOR, false);
    }
    public static void setMeteor(Context c, boolean v) {
        SharedPreferences.Editor e = prefs(c).edit();
        e.putBoolean(KEY_METEOR, v);
        if (v) e.putBoolean(KEY_SNOW, false);
        e.apply();
    }

    // ===== 服务运行状态 =====
    public static boolean isServiceRunning(Context c) {
        return prefs(c).getBoolean(KEY_SERVICE_RUNNING, false);
    }
    public static void setServiceRunning(Context c, boolean v) {
        prefs(c).edit().putBoolean(KEY_SERVICE_RUNNING, v).apply();
    }

    // ===== 文本替换核心（无障碍服务调用）=====
    public static String apply(Context c, String orig) {
        if (orig == null) return null;
        String s = orig;
        if (isYouToMaster(c)) s = s.replace("你", "主人").replace("您", "主人");
        if (isIToMe(c))      s = s.replace("我", "本喵").replace("吾", "本喵");
        if (isAddNya(c)) {
            if (!s.endsWith("喵") && !s.endsWith("~")) s = s + "喵~";
        }
        return s;
    }
}

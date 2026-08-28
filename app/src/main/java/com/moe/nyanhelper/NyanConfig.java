package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREFS = "nyan_helper_prefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_ADD_NYA = "add_nya";
    private static final String KEY_REPLACE_YOU = "replace_you";
    private static final String KEY_REPLACE_ME = "replace_me";
    private static final String KEY_SNOW = "snow";
    private static final String KEY_METEOR = "meteor";
    private static final String KEY_SERVICE_RUNNING = "service_running";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ===== 主题 =====
    public static int getTheme(Context context) {
        return prefs(context).getInt(KEY_THEME, 0);
    }

    public static void setTheme(Context context, int theme) {
        prefs(context).edit().putInt(KEY_THEME, theme).apply();
    }

    // ===== 句尾加喵 =====
    public static boolean isAddNya(Context context) {
        return prefs(context).getBoolean(KEY_ADD_NYA, false);
    }

    public static void setAddNya(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_ADD_NYA, value).apply();
    }

    // ===== 你→主人 =====
    public static boolean isReplaceYou(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE_YOU, false);
    }

    public static void setReplaceYou(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_REPLACE_YOU, value).apply();
    }

    // ===== 我→本喵 =====
    public static boolean isReplaceMe(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE_ME, false);
    }

    public static void setReplaceMe(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_REPLACE_ME, value).apply();
    }

    // ===== 雪花特效 =====
    public static boolean isSnow(Context context) {
        return prefs(context).getBoolean(KEY_SNOW, false);
    }

    public static void setSnow(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_SNOW, value).apply();
    }

    // ===== 流星特效 =====
    public static boolean isMeteor(Context context) {
        return prefs(context).getBoolean(KEY_METEOR, false);
    }

    public static void setMeteor(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_METEOR, value).apply();
    }

    // ===== 服务运行状态 =====
    public static boolean isServiceRunning(Context context) {
        return prefs(context).getBoolean(KEY_SERVICE_RUNNING, false);
    }

    public static void setServiceRunning(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_SERVICE_RUNNING, value).apply();
    }

    // ===== 文本替换（给无障碍服务用）=====
    public static String apply(Context context, String orig) {
        if (orig == null) return null;

        String replaced = orig;

        if (isReplaceYou(context)) {
            replaced = replaced.replace("你", "主人")
                               .replace("您", "主人");
        }

        if (isReplaceMe(context)) {
            replaced = replaced.replace("我", "本喵")
                               .replace("吾", "本喵");
        }

        if (isAddNya(context)) {
            if (!replaced.endsWith("喵") && !replaced.endsWith("~")) {
                replaced = replaced + "喵~";
            }
        }

        return replaced;
    }
}

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
    private static final String KEY_REPLACE_YOU = "replace_you";
    private static final String KEY_REPLACE_ME = "replace_me";
    private static final String KEY_YOU_TO_MASTER = "you_to_master";
    private static final String KEY_I_TO_ME = "i_to_me";
    private static final String KEY_SNOW = "snow";
    private static final String KEY_METEOR = "meteor";
    private static final String KEY_SERVICE_RUNNING = "service_running";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ===== 主题 =====
    public static int getTheme(Context context) {
        return prefs(context).getInt(KEY_THEME, THEME_SAKURA);
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

    // ===== 你→主人（兼容两套名字）=====
    public static boolean isReplaceYou(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE_YOU, false)
                || prefs(context).getBoolean(KEY_YOU_TO_MASTER, false);
    }

    public static void setReplaceYou(Context context, boolean value) {
        prefs(context).edit()
                .putBoolean(KEY_REPLACE_YOU, value)
                .putBoolean(KEY_YOU_TO_MASTER, value)
                .apply();
    }

    public static boolean isYouToMaster(Context context) {
        return isReplaceYou(context);
    }

    public static void setYouToMaster(Context context, boolean value) {
        setReplaceYou(context, value);
    }

    // ===== 我→本喵（兼容两套名字）=====
    public static boolean isReplaceMe(Context context) {
        return prefs(context).getBoolean(KEY_REPLACE_ME, false)
                || prefs(context).getBoolean(KEY_I_TO_ME, false);
    }

    public static void setReplaceMe(Context context, boolean value) {
        prefs(context).edit()
                .putBoolean(KEY_REPLACE_ME, value)
                .putBoolean(KEY_I_TO_ME, value)
                .apply();
    }

    public static boolean isIToMe(Context context) {
        return isReplaceMe(context);
    }

    public static void setIToMe(Context context, boolean value) {
        setReplaceMe(context, value);
    }

    // ===== 雪花特效 =====
    public static boolean isSnow(Context context) {
        return prefs(context).getBoolean(KEY_SNOW, false);
    }

    public static void setSnow(Context context, boolean value) {
        SharedPreferences.Editor e = prefs(context).edit();
        e.putBoolean(KEY_SNOW, value);
        if (value) e.putBoolean(KEY_METEOR, false); // 互斥
        e.apply();
    }

    // ===== 流星特效 =====
    public static boolean isMeteor(Context context) {
        return prefs(context).getBoolean(KEY_METEOR, false);
    }

    public static void setMeteor(Context context, boolean value) {
        SharedPreferences.Editor e = prefs(context).edit();
        e.putBoolean(KEY_METEOR, value);
        if (value) e.putBoolean(KEY_SNOW, false); // 互斥
        e.apply();
    }

    // ===== 服务运行状态 =====
    public static boolean isServiceRunning(Context context) {
        return prefs(context).getBoolean(KEY_SERVICE_RUNNING, false);
    }

    public static void setServiceRunning(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_SERVICE_RUNNING, value).apply();
    }

    // ===== 文本替换核心（无障碍服务调用）=====
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

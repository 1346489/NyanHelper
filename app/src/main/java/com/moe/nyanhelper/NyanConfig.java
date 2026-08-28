package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREFS = "nyan_config";
    private static final String KEY_SERVICE_RUNNING = "service_running";
    private static final String KEY_ADD_NYA = "add_nya";
    private static final String KEY_YOU_TO_MASTER = "you_to_master";
    private static final String KEY_I_TO_ME = "i_to_me";
    private static final String KEY_SNOW = "snow";
    private static final String KEY_METEOR = "meteor";
    private static final String KEY_THEME = "theme";

    // ===== 服务运行状态 =====
    public static void setServiceRunning(Context context, boolean running) {
        getSp(context).edit().putBoolean(KEY_SERVICE_RUNNING, running).apply();
    }

    public static boolean isServiceRunning(Context context) {
        return getSp(context).getBoolean(KEY_SERVICE_RUNNING, false);
    }

    // ===== 功能开关 =====
    public static void setAddNya(Context context, boolean on) {
        getSp(context).edit().putBoolean(KEY_ADD_NYA, on).apply();
    }

    public static boolean isAddNya(Context context) {
        return getSp(context).getBoolean(KEY_ADD_NYA, true);
    }

    public static void setYouToMaster(Context context, boolean on) {
        getSp(context).edit().putBoolean(KEY_YOU_TO_MASTER, on).apply();
    }

    public static boolean isYouToMaster(Context context) {
        return getSp(context).getBoolean(KEY_YOU_TO_MASTER, true);
    }

    public static void setIToMe(Context context, boolean on) {
        getSp(context).edit().putBoolean(KEY_I_TO_ME, on).apply();
    }

    public static boolean isIToMe(Context context) {
        return getSp(context).getBoolean(KEY_I_TO_ME, true);
    }

    // ===== 特效开关（互斥）=====
    public static void setSnow(Context context, boolean on) {
        SharedPreferences.Editor e = getSp(context).edit();
        e.putBoolean(KEY_SNOW, on);
        if (on) e.putBoolean(KEY_METEOR, false);
        e.apply();
    }

    public static boolean isSnow(Context context) {
        return getSp(context).getBoolean(KEY_SNOW, false);
    }

    public static void setMeteor(Context context, boolean on) {
        SharedPreferences.Editor e = getSp(context).edit();
        e.putBoolean(KEY_METEOR, on);
        if (on) e.putBoolean(KEY_SNOW, false);
        e.apply();
    }

    public static boolean isMeteor(Context context) {
        return getSp(context).getBoolean(KEY_METEOR, false);
    }

    // ===== 主题 =====
    public static final int THEME_SAKURA = 0;
    public static final int THEME_MINT = 1;
    public static final int THEME_STARRY = 2;

    public static void setTheme(Context context, int theme) {
        getSp(context).edit().putInt(KEY_THEME, theme).apply();
    }

    public static int getTheme(Context context) {
        return getSp(context).getInt(KEY_THEME, THEME_SAKURA);
    }

    // ===== 文字替换核心 =====
    public static String apply(Context context, String text) {
        if (text == null) return null;
        String result = text;
        if (isYouToMaster(context)) result = result.replace("你", "主人");
        if (isIToMe(context)) result = result.replace("我", "本喵");
        if (isAddNya(context)) result = result + "喵~";
        return result;
    }

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 全局配置：三个功能开关 + 两个特效开关（互斥）+ 主题背景色
 */
public class NyanConfig {

    private static final String PREF = "nyan_config";

    // 功能页：三个开关
    public static final String KEY_ADD_NYA = "add_nya";       // 结尾加"喵"
    public static final String KEY_ME = "me";                  // "我" -> "本喵"
    public static final String KEY_YOU = "you";                // "你" -> "主人"

    // 设置页：两个互斥特效开关
    public static final String KEY_SNOW = "snow";              // 雪花
    public static final String KEY_METEOR = "meteor";          // 流星

    // 主题页：背景色（三个预设）
    public static final String KEY_THEME = "theme";            // 0 / 1 / 2

    // 悬浮窗运行状态
    public static final String KEY_FLOAT_STARTED = "float_started";

    /* ========== 功能开关 ========== */

    public static boolean isAddNya(Context c) { return pref(c).getBoolean(KEY_ADD_NYA, false); }
    public static void setAddNya(Context c, boolean v) { pref(c).edit().putBoolean(KEY_ADD_NYA, v).apply(); }

    public static boolean isMe(Context c) { return pref(c).getBoolean(KEY_ME, false); }
    public static void setMe(Context c, boolean v) { pref(c).edit().putBoolean(KEY_ME, v).apply(); }

    public static boolean isYou(Context c) { return pref(c).getBoolean(KEY_YOU, false); }
    public static void setYou(Context c, boolean v) { pref(c).edit().putBoolean(KEY_YOU, v).apply(); }

    /* ========== 特效开关（互斥） ========== */

    public static boolean isSnow(Context c) { return pref(c).getBoolean(KEY_SNOW, false); }
    public static boolean isMeteor(Context c) { return pref(c).getBoolean(KEY_METEOR, false); }

    /** 开雪花 -> 自动关流星 */
    public static void setSnow(Context c, boolean v) {
        SharedPreferences.Editor e = pref(c).edit();
        e.putBoolean(KEY_SNOW, v);
        if (v) e.putBoolean(KEY_METEOR, false);
        e.apply();
    }

    /** 开流星 -> 自动关雪花 */
    public static void setMeteor(Context c, boolean v) {
        SharedPreferences.Editor e = pref(c).edit();
        e.putBoolean(KEY_METEOR, v);
        if (v) e.putBoolean(KEY_SNOW, false);
        e.apply();
    }

    /* ========== 主题 ========== */

    /** 三个主题：0=樱花粉, 1=薄荷绿, 2=星空紫 */
    public static int getTheme(Context c) { return pref(c).getInt(KEY_THEME, 0); }
    public static void setTheme(Context c, int t) { pref(c).edit().putInt(KEY_THEME, t).apply(); }

    /** 主题对应的背景渐变起始色 */
    public static int themeStartColor(Context c) {
        switch (getTheme(c)) {
            case 1: return 0xFFE8F5E9; // 薄荷绿 - 浅
            case 2: return 0xFF1A1A3E; // 星空紫 - 深
            default: return 0xFFFFF0F6; // 樱花粉 - 浅
        }
    }
    public static int themeCenterColor(Context c) {
        switch (getTheme(c)) {
            case 1: return 0xFFC8E6C9;
            case 2: return 0xFF3D2C5A;
            default: return 0xFFFFE3EC;
        }
    }
    public static int themeEndColor(Context c) {
        switch (getTheme(c)) {
            case 1: return 0xFFA5D6A7;
            case 2: return 0xFF6A4C93;
            default: return 0xFFFFD1E8;
        }
    }
    /** 主题对应的主色（按钮、标题等） */
    public static int themePrimary(Context c) {
        switch (getTheme(c)) {
            case 1: return 0xFF4CAF50;
            case 2: return 0xFF9C6BC4;
            default: return 0xFFE91E8C;
        }
    }

    /* ========== 文字替换核心 ========== */

    public static String apply(String text, Context c) {
        if (text == null || text.trim().isEmpty()) return text;
        String s = text;
        if (isYou(c)) s = s.replace("你", "主人");
        if (isMe(c))  s = s.replace("我", "本喵");
        if (isAddNya(c)) s = addNya(s);
        return s;
    }

    /** 结尾加"喵"：句末标点前插入，否则直接追加 */
    private static String addNya(String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        if (s.endsWith("喵") || s.endsWith("喵~") || s.endsWith("喵！") || s.endsWith("喵喵")) return s;
        char last = s.charAt(s.length() - 1);
        if ("。！？….!?~".indexOf(last) >= 0) {
            return s.substring(0, s.length() - 1) + "喵" + last;
        }
        return s + "喵~";
    }

    public static boolean isFloatStarted(Context c) {
        return pref(c).getBoolean(KEY_FLOAT_STARTED, false);
    }
    public static void setFloatStarted(Context c, boolean v) {
        pref(c).edit().putBoolean(KEY_FLOAT_STARTED, v).apply();
    }

    private static SharedPreferences pref(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}

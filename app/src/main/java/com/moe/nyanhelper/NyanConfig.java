package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREF = "nyan_config";

    // 功能页开关
    public static boolean isAddNya(Context c) { return pref(c).getBoolean("add_nya", true); }
    public static void setAddNya(Context c, boolean v) { pref(c).edit().putBoolean("add_nya", v).apply(); }

    public static boolean isReplaceYou(Context c) { return pref(c).getBoolean("replace_you", true); }
    public static void setReplaceYou(Context c, boolean v) { pref(c).edit().putBoolean("replace_you", v).apply(); }

    public static boolean isReplaceMe(Context c) { return pref(c).getBoolean("replace_me", true); }
    public static void setReplaceMe(Context c, boolean v) { pref(c).edit().putBoolean("replace_me", v).apply(); }

    // 设置页开关（互斥：只能开一个）
    public static boolean isSnow(Context c) { return pref(c).getBoolean("snow", false); }
    public static boolean isMeteor(Context c) { return pref(c).getBoolean("meteor", false); }

    public static void setSnow(Context c, boolean on) {
        SharedPreferences.Editor e = pref(c).edit();
        e.putBoolean("snow", on);
        if (on) e.putBoolean("meteor", false);
        e.apply();
    }

    public static void setMeteor(Context c, boolean on) {
        SharedPreferences.Editor e = pref(c).edit();
        e.putBoolean("meteor", on);
        if (on) e.putBoolean("snow", false);
        e.apply();
    }

    // 主题：0=樱花粉 1=薄荷绿 2=星空紫
    public static int getTheme(Context c) { return pref(c).getInt("theme", 0); }
    public static void setTheme(Context c, int t) { pref(c).edit().putInt("theme", t).apply(); }

    public static int getThemeColor(Context c) {
        switch (getTheme(c)) {
            case 1: return 0xFFE8F5E9; // 薄荷绿
            case 2: return 0xFF2A2348; // 星空紫
            default: return 0xFFFFF0F6; // 樱花粉
        }
    }

    /**
     * 核心替换逻辑（不依赖具体 App，对任意聊天/输入类 EditText 生效）：
     *   你 → 主人
     *   我 → 本喵
     *   结尾加「喵」（在句末标点前）
     */
    public static String apply(String text) {
        if (text == null || text.isEmpty()) return text;
        String s = text;
        if (isReplaceYou(nullSafe())) s = s.replace("你", "主人");
        if (isReplaceMe(nullSafe())) s = s.replace("我", "本喵");
        if (isAddNya(nullSafe())) s = appendNya(s);
        return s;
    }

    private static String appendNya(String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        if (s.endsWith("喵") || s.endsWith("喵~") || s.endsWith("喵！")) return s;
        char last = s.charAt(s.length() - 1);
        if ("。！？….!?~".indexOf(last) >= 0) {
            return s.substring(0, s.length() - 1) + "喵" + last;
        }
        return s + "喵~";
    }

    private static Context nullSafe() {
        return AppContext.get();
    }

    private static SharedPreferences pref(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}

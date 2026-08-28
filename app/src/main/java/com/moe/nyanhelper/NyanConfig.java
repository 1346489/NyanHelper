package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREF = "nyan_config";

    // 功能页开关
    public static boolean isAddNya(Context c) { return pref(c).getBoolean("add_nya", false); }
    public static void setAddNya(Context c, boolean v) { pref(c).edit().putBoolean("add_nya", v).apply(); }

    public static boolean isMe(Context c) { return pref(c).getBoolean("me", false); }
    public static void setMe(Context c, boolean v) { pref(c).edit().putBoolean("me", v).apply(); }

    public static boolean isYou(Context c) { return pref(c).getBoolean("you", false); }
    public static void setYou(Context c, boolean v) { pref(c).edit().putBoolean("you", v).apply(); }

    // 设置页：雪花 / 流星（互斥）
    public static boolean isSnow(Context c) { return pref(c).getBoolean("snow", false); }
    public static void setSnow(Context c, boolean v) { pref(c).edit().putBoolean("snow", v).apply(); }

    public static boolean isMeteor(Context c) { return pref(c).getBoolean("meteor", false); }
    public static void setMeteor(Context c, boolean v) { pref(c).edit().putBoolean("meteor", v).apply(); }

    // 主题：0=粉樱, 1=星空紫, 2=薄荷
    public static int getTheme(Context c) { return pref(c).getInt("theme", 0); }
    public static void setTheme(Context c, int t) { pref(c).edit().putInt("theme", t).apply(); }

    /** 文本替换入口：统一在 AccessibilityService 调用 */
    public static String apply(String text, Context c) {
        if (text == null || text.trim().isEmpty()) return text;
        String s = text;
        if (isYou(c)) s = s.replace("你", "主人");
        if (isMe(c))  s = s.replace("我", "本喵");
        if (isAddNya(c)) s = addNya(s);
        return s;
    }

    private static String addNya(String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        if (s.endsWith("喵") || s.endsWith("喵~") || s.endsWith("喵！")) return s;
        char last = s.charAt(s.length() - 1);
        if ("。！？….!?~".indexOf(last) >= 0) return s.substring(0, s.length() - 1) + "喵" + last;
        return s + "喵~";
    }

    private static SharedPreferences pref(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}

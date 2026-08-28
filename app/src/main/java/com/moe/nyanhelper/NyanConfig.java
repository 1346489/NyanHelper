package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class NyanConfig {

    private static final String PREF = "nyan_config";

    public static boolean isAddNya(Context c) { return pref(c).getBoolean("add_nya", false); }
    public static void setAddNya(Context c, boolean v) { pref(c).edit().putBoolean("add_nya", v).apply(); }

    public static boolean isMe(Context c) { return pref(c).getBoolean("me", false); }
    public static void setMe(Context c, boolean v) { pref(c).edit().putBoolean("me", v).apply(); }

    public static boolean isYou(Context c) { return pref(c).getBoolean("you", false); }
    public static void setYou(Context c, boolean v) { pref(c).edit().putBoolean("you", v).apply(); }

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

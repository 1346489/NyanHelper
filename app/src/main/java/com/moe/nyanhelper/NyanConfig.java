package com.moe.nyanhelper;

public class NyanConfig {
    public static boolean addNya = true;
    public static boolean snow = false;
    public static boolean meteor = false;

    public static void setSnow(boolean on) {
        snow = on;
        if (on) meteor = false;
    }

    public static void setMeteor(boolean on) {
        meteor = on;
        if (on) snow = false;
    }

    public static String apply(String text) {
        if (text == null) return null;
        String result = text.replace("你", "主人").replace("我", "本喵");
        if (addNya) result += "喵~";
        return result;
    }
}

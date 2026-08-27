package com.moe.nyanhelper;

public class NyanText {
    public static String transform(String text, boolean addTail, boolean replaceMe, boolean replaceYou) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String s = text;
        if (replaceMe) {
            s = s.replace("我", "本喵");
        }
        if (replaceYou) {
            s = s.replace("你", "主人");
        }
        if (addTail && !s.endsWith("喵")) {
            s = s + "喵";
        }
        return s;
    }
}

package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    public static final int THEME_SAKURA = 0; // 樱花粉
    public static final int THEME_MINT   = 1; // 薄荷绿
    public static final int THEME_STARRY = 2; // 星空紫

    private static final String PREF = "nyan_theme";
    private static final String KEY  = "theme_index";

    public static void setTheme(Context c, int theme) {
        prefs(c).edit().putInt(KEY, theme).apply();
    }

    public static int getTheme(Context c) {
        return prefs(c).getInt(KEY, THEME_SAKURA);
    }

    // 主背景渐变（用于 Activity 根布局）
    public static int getMainBackground(Context c) {
        switch (getTheme(c)) {
            case THEME_MINT:   return R.drawable.bg_theme_mint;
            case THEME_STARRY: return R.drawable.bg_theme_starry;
            default:           return R.drawable.bg_theme_sakura;
        }
    }

    // 卡片/面板背景色（ARGB int，用于动态着色）
    public static int getCardColor(Context c) {
        switch (getTheme(c)) {
            case THEME_MINT:   return 0xFFE8F8F0; // 薄荷白
            case THEME_STARRY: return 0xFF2B2440; // 深紫
            default:           return 0xFFFFFFFF; // 樱花粉-白
        }
    }

    public static int getTextColor(Context c) {
        return getTheme(c) == THEME_STARRY ? 0xFFEDE7F6 : 0xFF5A2E4A;
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}

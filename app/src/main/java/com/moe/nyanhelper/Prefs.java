package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "nyan_prefs";
    private static final String KEY_FLOAT = "float_on";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static boolean isFloatOn(Context c) {
        return sp(c).getBoolean(KEY_FLOAT, false);
    }

    public static void setFloatOn(Context c, boolean on) {
        sp(c).edit().putBoolean(KEY_FLOAT, on).apply();
    }
}

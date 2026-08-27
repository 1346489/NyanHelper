package com.moe.nyanhelper;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "nyan_prefs";
    private static final String KEY_FLOAT = "float_on";
    private static final String KEY_TAIL = "add_meow_tail";
    private static final String KEY_ME = "replace_me";
    private static final String KEY_YOU = "replace_you";

    private static SharedPreferences sp(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static boolean isFloatOn(Context ctx) {
        return sp(ctx).getBoolean(KEY_FLOAT, false);
    }

    public static void setFloatOn(Context ctx, boolean v) {
        sp(ctx).edit().putBoolean(KEY_FLOAT, v).apply();
    }

    public static boolean addMeowTail(Context ctx) {
        return sp(ctx).getBoolean(KEY_TAIL, true);
    }

    public static void setAddMeowTail(Context ctx, boolean v) {
        sp(ctx).edit().putBoolean(KEY_TAIL, v).apply();
    }

    public static boolean replaceMe(Context ctx) {
        return sp(ctx).getBoolean(KEY_ME, true);
    }

    public static void setReplaceMe(Context ctx, boolean v) {
        sp(ctx).edit().putBoolean(KEY_ME, v).apply();
    }

    public static boolean replaceYou(Context ctx) {
        return sp(ctx).getBoolean(KEY_YOU, true);
    }

    public static void setReplaceYou(Context ctx, boolean v) {
        sp(ctx).edit().putBoolean(KEY_YOU, v).apply();
    }
}

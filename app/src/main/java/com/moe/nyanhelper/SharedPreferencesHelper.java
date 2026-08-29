package com.benmao.assistant;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "benmao_prefs";
    private SharedPreferences prefs;

    private boolean funcAddMiao;
    private boolean funcReplaceMe;
    private boolean funcReplaceYou;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        reload();
    }

    public void reload() {
        funcAddMiao = prefs.getBoolean("func_add_miao", false);
        funcReplaceMe = prefs.getBoolean("func_replace_me", false);
        funcReplaceYou = prefs.getBoolean("func_replace_you", false);
    }

    public boolean isFuncAddMiao() { return funcAddMiao; }
    public boolean isFuncReplaceMe() { return funcReplaceMe; }
    public boolean isFuncReplaceYou() { return funcReplaceYou; }
}

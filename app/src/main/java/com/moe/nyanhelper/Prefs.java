package com.benmao.assistant;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "benmao_prefs";
    private final SharedPreferences sp;

    public Prefs(Context context) {
        this.sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    // 功能开关
    public boolean isAddMeow() { return sp.getBoolean("add_meow", false); }
    public void setAddMeow(boolean v) { sp.edit().putBoolean("add_meow", v).apply(); }
    public boolean isReplaceMe() { return sp.getBoolean("replace_me", false); }
    public void setReplaceMe(boolean v) { sp.edit().putBoolean("replace_me", v).apply(); }
    public boolean isReplaceYou() { return sp.getBoolean("replace_you", false); }
    public void setReplaceYou(boolean v) { sp.edit().putBoolean("replace_you", v).apply(); }

    // 粒子
    public boolean isSnow() { return sp.getBoolean("snow", false); }
    public void setSnow(boolean v) { sp.edit().putBoolean("snow", v).apply(); }
    public boolean isMeteor() { return sp.getBoolean("meteor", false); }
    public void setMeteor(boolean v) { sp.edit().putBoolean("meteor", v).apply(); }

    // 控制
    public boolean isVolumeHide() { return sp.getBoolean("volume_hide", true); }
    public void setVolumeHide(boolean v) { sp.edit().putBoolean("volume_hide", v).apply(); }
}

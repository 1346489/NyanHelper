package com.moe.nyanhelper;

import android.content.Context;
import android.view.View;

public class ThemeManager {

    public static void apply(Context context, View rootView) {
        if (context == null || rootView == null) return;
        int theme = NyanConfig.getTheme(context);
        switch (theme) {
            case NyanConfig.THEME_MINT:
                rootView.setBackgroundResource(R.drawable.bg_theme_mint);
                break;
            case NyanConfig.THEME_STARRY:
                rootView.setBackgroundResource(R.drawable.bg_theme_starry);
                break;
            case NyanConfig.THEME_SAKURA:
            default:
                rootView.setBackgroundResource(R.drawable.bg_theme_sakura);
                break;
        }
    }
}

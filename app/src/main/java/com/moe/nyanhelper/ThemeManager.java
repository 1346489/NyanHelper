package com.moe.nyanhelper;

import android.content.Context;
import android.view.View;

public class ThemeManager {

    // 与 NyanConfig 主题常量一致
    public static final int THEME_SAKURA = NyanConfig.THEME_SAKURA;
    public static final int THEME_MINT = NyanConfig.THEME_MINT;
    public static final int THEME_STARRY = NyanConfig.THEME_STARRY;

    /**
     * 给 Activity 根布局设置主题背景。
     * @param context 上下文
     * @param rootView 根布局（需有 id @+id/root）
     */
    public static void apply(Context context, View rootView) {
        if (context == null || rootView == null) return;

        int theme = NyanConfig.getTheme(context);
        switch (theme) {
            case THEME_MINT:
                rootView.setBackgroundResource(R.drawable.bg_theme_mint);
                break;
            case THEME_STARRY:
                rootView.setBackgroundResource(R.drawable.bg_theme_starry);
                break;
            case THEME_SAKURA:
            default:
                rootView.setBackgroundResource(R.drawable.bg_theme_sakura);
                break;
        }
    }

    /** 供 ImageView / 缩略图使用 */
    public static int getThemeBackgroundRes(Context context) {
        int theme = NyanConfig.getTheme(context);
        switch (theme) {
            case THEME_MINT: return R.drawable.bg_theme_mint;
            case THEME_STARRY: return R.drawable.bg_theme_starry;
            case THEME_SAKURA:
            default: return R.drawable.bg_theme_sakura;
        }
    }
}

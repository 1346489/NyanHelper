package com.moe.nyanhelper;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class ThemeManager {

    public static final int THEME_SAKURA = 0;
    public static final int THEME_MINT = 1;
    public static final int THEME_STARRY = 2;

    /**
     * 给 Activity 根布局设置主题背景。
     * rootView 建议传 MainActivity 的 setContentView 根布局，例如：
     * View rootView = findViewById(android.R.id.content);
     * 或你布局里指定的根 LinearLayout/ConstraintLayout/FrameLayout。
     */
    public static void applyTheme(Context context, View rootView) {
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

        // 如果以后有状态栏/文字颜色适配，可以放这里。
        // 星空主题建议文字浅色，樱花粉/薄荷绿建议深色文字。
    }

    /**
     * 如果浮窗/面板里需要取背景 drawable id，保留这个。
     */
    public static int getThemeBackgroundRes(Context context) {
        int theme = NyanConfig.getTheme(context);

        switch (theme) {
            case THEME_MINT:
                return R.drawable.bg_theme_mint;
            case THEME_STARRY:
                return R.drawable.bg_theme_starry;
            case THEME_SAKURA:
            default:
                return R.drawable.bg_theme_sakura;
        }
    }

    /**
     * 示例：如果某个 ImageView 要显示主题背景缩略图/全屏背景，用这个。
     */
    public static void setImageViewBackground(Context context, ImageView imageView) {
        if (context == null || imageView == null) return;
        imageView.setImageResource(getThemeBackgroundRes(context));
    }
}

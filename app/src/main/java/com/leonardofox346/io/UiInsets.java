package com.leonardofox346.io;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 边到边（edge-to-edge）留白工具。
 *
 * <p>targetSdk 35（Android 15）起系统强制应用边到边显示，状态栏与导航栏不再为内容让位；
 * targetSdk 37 更是如此。因此每个界面都要自己把系统栏与刘海的高度补成内边距，
 * 否则标题栏会被状态栏压住、底部按钮会被导航栏盖住。</p>
 *
 * <p>只处理 padding，不消费 insets —— 返回原始 insets，让子 View 仍能收到分发。</p>
 */
final class UiInsets {

    private UiInsets() {
    }

    /** 四边都留白（用于没有自带底部栏的普通页面根布局）。 */
    static void applyTo(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = systemBars(windowInsets);
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }

    /** 只留上方与左右（下方交给别的控件，例如主界面底部操作栏）。 */
    static void applyTopAndSidesTo(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = systemBars(windowInsets);
            v.setPadding(bars.left, bars.top, bars.right, v.getPaddingBottom());
            return windowInsets;
        });
    }

    /** 只把底部系统栏高度补进去（控件自身的背景因此能延伸至导航栏后面）。 */
    static void applyBottomTo(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = systemBars(windowInsets);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return windowInsets;
        });
    }

    private static Insets systemBars(WindowInsetsCompat windowInsets) {
        return windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
    }
}

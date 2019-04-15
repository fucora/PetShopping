package cn.lovepet.shops.helper.immersive.title.style;


import cn.lovepet.shops.R;

/**
 *    desc   : 默认透明主题样式实现（布局属性：app:bar_style="transparent"）
 */
public class TitleBarTransparentStyle extends TitleBarNightStyle {

    @Override
    public int getBackgroundColor() {
        return 0x00000000;
    }

    @Override
    public int getLeftViewColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getTitleViewColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getRightViewColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public boolean getLineVisibility() {
        return false;
    }

    @Override
    public int getLineBackgroundColor() {
        return 0x00000000;
    }

    @Override
    public int getLeftViewBackground() {
        return R.drawable.bar_selector_selectable_transparent;
    }

    @Override
    public int getRightViewBackground() {
        return R.drawable.bar_selector_selectable_transparent;
    }
}
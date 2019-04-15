package cn.lovepet.shops.helper.immersive.title.style;


import cn.lovepet.shops.R;

/**
 *    desc   : 默认夜间主题样式实现（布局属性：app:bar_style="night"）
 */
public class TitleBarNightStyle extends TitleBarLightStyle {

    @Override
    public int getBackgroundColor() {
        return 0xFF000000;
    }

    @Override
    public int getBackIconResource() {
        return R.mipmap.bar_icon_back_white;
    }

    @Override
    public int getLeftViewColor() {
        return 0xCCFFFFFF;
    }

    @Override
    public int getTitleViewColor() {
        return 0xEEFFFFFF;
    }

    @Override
    public int getRightViewColor() {
        return 0x99FFFFFF;
    }

    @Override
    public int getLineBackgroundColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getLeftViewBackground() {
        return R.drawable.bar_selector_selectable_black;
    }

    @Override
    public int getRightViewBackground() {
        return R.drawable.bar_selector_selectable_black;
    }
}
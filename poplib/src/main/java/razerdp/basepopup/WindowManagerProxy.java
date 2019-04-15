package razerdp.basepopup;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import razerdp.util.log.LogTag;
import razerdp.util.log.PopupLogUtil;

/**
 * Created by 大灯泡 on 2017/12/25.
 * <p>
 * 代理掉popup的windowmanager，在addView操作，拦截decorView的操作
 */
final class WindowManagerProxy implements WindowManager {
    private static final String TAG = "WindowManagerProxy";
    private WindowManager mWindowManager;
    private WeakReference<PopupDecorViewProxy> mHackPopupDecorView;
    private WeakReference<BasePopupHelper> mPopupHelper;
    private static int statusBarHeight;

    public WindowManagerProxy(WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    @Override
    public Display getDefaultDisplay() {
        return mWindowManager == null ? null : mWindowManager.getDefaultDisplay();
    }

    @Override
    public void removeViewImmediate(View view) {
        PopupLogUtil.trace(LogTag.i, TAG, "WindowManager.removeViewImmediate  >>>  " + (view == null ? null : view.getClass().getSimpleName()));
        if (mWindowManager == null || view == null) return;
        checkStatusBarHeight(view.getContext());
        if (isPopupInnerDecorView(view) && getPopupDecorViewProxy() != null) {
            PopupDecorViewProxy popupDecorViewProxy = getPopupDecorViewProxy();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!popupDecorViewProxy.isAttachedToWindow()) return;
            }
            mWindowManager.removeViewImmediate(popupDecorViewProxy);
            mHackPopupDecorView.clear();
            mHackPopupDecorView = null;
        } else {
            mWindowManager.removeViewImmediate(view);
        }
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams params) {
        PopupLogUtil.trace(LogTag.i, TAG, "WindowManager.addView  >>>  " + (view == null ? null : view.getClass().getSimpleName()));
        if (mWindowManager == null || view == null) return;
        checkStatusBarHeight(view.getContext());
        if (isPopupInnerDecorView(view)) {
            /**
             * 此时的params是WindowManager.LayoutParams，需要留意强转问题
             * popup内部有scrollChangeListener，会有params强转为WindowManager.LayoutParams的情况
             */
            BasePopupHelper helper = getBasePopupHelper();

            applyHelper(params, helper);
            //添加popup主体
            final PopupDecorViewProxy popupDecorViewProxy = PopupDecorViewProxy.create(view.getContext(), helper);
            popupDecorViewProxy.addPopupDecorView(view, (LayoutParams) params);
            mHackPopupDecorView = new WeakReference<PopupDecorViewProxy>(popupDecorViewProxy);
            mWindowManager.addView(popupDecorViewProxy, fitLayoutParamsPosition(params));
        } else {
            mWindowManager.addView(view, params);
        }
    }

    private void applyHelper(ViewGroup.LayoutParams params, BasePopupHelper helper) {
        if (params instanceof LayoutParams && helper != null) {
            LayoutParams p = (LayoutParams) params;
            if (!helper.isInterceptTouchEvent()) {
                PopupLogUtil.trace(LogTag.i, TAG, "applyHelper  >>>  不拦截事件");
                p.flags |= LayoutParams.FLAG_NOT_TOUCH_MODAL;
                p.flags |= LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                if (!helper.isClipToScreen()) {
                    p.flags |= LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                }
            }
            if (helper.isFullScreen()) {
                PopupLogUtil.trace(LogTag.i, TAG, "applyHelper  >>>  全屏");
                p.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                if (helper.isInterceptTouchEvent()) {
                    p.flags |= LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                }
            }
        }
    }

    private ViewGroup.LayoutParams fitLayoutParamsPosition(ViewGroup.LayoutParams params) {
        if (params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams p = (LayoutParams) params;
            BasePopupHelper helper = getBasePopupHelper();
            if (helper != null) {
                if (helper.getShowCount() > 1) {
                    p.type = LayoutParams.TYPE_APPLICATION_SUB_PANEL;
                }
                if (helper.isInterceptTouchEvent()) {
                    //偏移交给PopupDecorViewProxy处理，此处固定为0
                    p.y = 0;
                    p.x = 0;
                } else {
                    if (helper.isShowAsDropDown()) {
                        Point offset = helper.getCachedOffset();
                        p.x += offset.x;
                        p.y += offset.y;
                        Log.d(TAG, "fitLayoutParamsPosition: x = " + p.x + "  y = " + p.y + "  offsetX = " + offset.x + "  offsetY = " + offset.y);
                    }
                }
            }
            applyHelper(p, helper);
        }
        return params;
    }

    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        PopupLogUtil.trace(LogTag.i, TAG, "WindowManager.updateViewLayout  >>>  " + (view == null ? null : view.getClass().getSimpleName()));
        if (mWindowManager == null || view == null) return;
        checkStatusBarHeight(view.getContext());
        if (isPopupInnerDecorView(view) && getPopupDecorViewProxy() != null) {
            PopupDecorViewProxy popupDecorViewProxy = getPopupDecorViewProxy();
            mWindowManager.updateViewLayout(popupDecorViewProxy, fitLayoutParamsPosition(params));
        } else {
            mWindowManager.updateViewLayout(view, params);
        }
    }

    public void update() {
        if (mWindowManager == null) return;
        if (getPopupDecorViewProxy() != null) {
            getPopupDecorViewProxy().updateLayout();
        }
    }

    @Override
    public void removeView(View view) {
        PopupLogUtil.trace(LogTag.i, TAG, "WindowManager.removeView  >>>  " + (view == null ? null : view.getClass().getSimpleName()));
        if (mWindowManager == null || view == null) return;
        checkStatusBarHeight(view.getContext());
        if (isPopupInnerDecorView(view) && getPopupDecorViewProxy() != null) {
            PopupDecorViewProxy popupDecorViewProxy = getPopupDecorViewProxy();
            mWindowManager.removeView(popupDecorViewProxy);
            mHackPopupDecorView.clear();
            mHackPopupDecorView = null;
        } else {
            mWindowManager.removeView(view);
        }
    }

    public void clear() {
        try {
            removeViewImmediate(mHackPopupDecorView.get());
            mHackPopupDecorView.clear();
        } catch (Exception e) {
            //no print
        }
    }

    private boolean isPopupInnerDecorView(View v) {
        if (v == null) return false;
        String viewSimpleClassName = v.getClass().getSimpleName();
        return TextUtils.equals(viewSimpleClassName, "PopupDecorView") || TextUtils.equals(viewSimpleClassName, "PopupViewContainer");
    }

    private PopupDecorViewProxy getPopupDecorViewProxy() {
        if (mHackPopupDecorView == null) return null;
        return mHackPopupDecorView.get();
    }


    private BasePopupHelper getBasePopupHelper() {
        if (mPopupHelper == null) return null;
        return mPopupHelper.get();
    }

    void bindPopupHelper(BasePopupHelper helper) {
        mPopupHelper = new WeakReference<BasePopupHelper>(helper);
    }

    private void checkStatusBarHeight(Context context) {
        if (statusBarHeight != 0 || context == null) return;
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        statusBarHeight = result;
    }

}

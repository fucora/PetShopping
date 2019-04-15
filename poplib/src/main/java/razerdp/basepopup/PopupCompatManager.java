package razerdp.basepopup;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import razerdp.util.log.PopupLogUtil;

/**
 * Created by 大灯泡 on 2017/11/27.
 * <p>
 * PopupWindow兼容类
 */

final class PopupCompatManager {
    private static final String TAG = "PopupCompatManager";

    private static final PopupWindowImpl IMPL;

    public static void showAsDropDown(BasePopupWindowProxy popupWindow, View anchor, int xoff, int yoff, int gravity) {
        if (IMPL != null) {
            IMPL.showAsDropDown(popupWindow, anchor, xoff, yoff, gravity);
        }
    }

    public static void showAtLocation(BasePopupWindowProxy popupWindow, View parent, int gravity, int x, int y) {
        if (IMPL != null) {
            IMPL.showAtLocation(popupWindow, parent, gravity, x, y);
        }
    }


    //-----------------------------------------compat-----------------------------------------
    interface PopupWindowImpl {
        void showAsDropDown(BasePopupWindowProxy popupWindow, View anchor, int xoff, int yoff, int gravity);

        void showAtLocation(BasePopupWindowProxy popupWindow, View parent, int gravity, int x, int y);
    }

    static {
        IMPL = new Impl();
    }

    //base impl
    static abstract class BaseImpl implements PopupWindowImpl {

        abstract void showAsDropDownImpl(Activity activity, BasePopupWindowProxy popupWindow, View anchor, int xoff, int yoff, int gravity);

        abstract void showAtLocationImpl(Activity activity, BasePopupWindowProxy popupWindow, View parent, int gravity, int x, int y);

        @Override
        public void showAsDropDown(BasePopupWindowProxy popupWindow, View anchor, int xoff, int yoff, int gravity) {
            if (isPopupShowing(popupWindow)) return;
            Activity activity = popupWindow.scanForActivity(anchor.getContext());
            if (activity == null) {
                Log.e(TAG, "please make sure that context is instance of activity");
                return;
            }
            onBeforeShowExec(popupWindow, activity);
            showAsDropDownImpl(activity, popupWindow, anchor, xoff, yoff, gravity);
            onAfterShowExec(popupWindow, activity);
        }

        @Override
        public void showAtLocation(BasePopupWindowProxy popupWindow, View parent, int gravity, int x, int y) {
            if (isPopupShowing(popupWindow)) return;
            Activity activity = popupWindow.scanForActivity(parent.getContext());
            if (activity == null) {
                Log.e(TAG, "please make sure that context is instance of activity");
                return;
            }
            onBeforeShowExec(popupWindow, activity);
            showAtLocationImpl(activity, popupWindow, parent, gravity, x, y);
            onAfterShowExec(popupWindow, activity);
        }


        protected void onBeforeShowExec(BasePopupWindowProxy popupWindowProxy, Activity act) {
//            fitSystemBar(act);
        }

        protected void onAfterShowExec(BasePopupWindowProxy popupWindowProxy, Activity act) {

        }

        boolean isPopupShowing(BasePopupWindowProxy popupWindow) {
            return popupWindow != null && popupWindow.callSuperIsShowing();
        }

        /**
         * 修复popup显示时退出沉浸的问题
         * 如果activity是全屏的，则popupwindow的window也应该
         */
        void fitSystemBar(Activity act) {
            if (act == null) return;
            try {
                WindowManager.LayoutParams lp = act.getWindow().getAttributes();
                int i = lp.flags;
                if ((i & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
                    //full screen
                    hideSystemUI(act);
                } else {
                    //showSystemUI(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void hideSystemUI(final Activity act) {
            try {

                int newUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                }


                act.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
                PopupLogUtil.trace("hideSystemBar");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void showSystemUI(View view) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    view.setSystemUiVisibility(uiOptions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    static class Impl extends BaseImpl {
        int[] anchorLocation = new int[2];

        @Override
        void showAsDropDownImpl(Activity activity, BasePopupWindowProxy popupWindow, View anchor, int xoff, int yoff, int gravity) {
            if (anchor != null) {
                anchor.getLocationInWindow(anchorLocation);
                xoff = anchorLocation[0];
                yoff = anchorLocation[1] + anchor.getHeight();
            }
            //offset在basepopupwindow已经计算好，在windowmanagerproxy里面进行适配
            popupWindow.callSuperShowAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, xoff, yoff);
        }

        @Override
        void showAtLocationImpl(Activity activity, BasePopupWindowProxy popupWindow, View parent, int gravity, int x, int y) {
            popupWindow.callSuperShowAtLocation(parent, gravity, x, y);
        }
    }
}

package com.lxyx.helllib;

/**
 * Created by habbyge on 2019/03/30.
 * <p>
 * 定义使用到的常量.
 */
final class HellConstant {

    private HellConstant() {
    }

    // activity事件
    public static final int Page_Event_Invalidate = -1;
    public static final int Page_Event_OnCreate = 0;
    public static final int Page_Event_OnNewIntent = 1;
    public static final int Page_Event_OnResume = 2;
    public static final int Page_Event_OnPause = 3;
    public static final int Page_Event_OnStop = 4;
    public static final int Page_Event_OnDestroy = 5;
    public static final int Page_Event_OnPostResume = 6;

    public static String getFragmentEventName(int eventType) {
        switch (eventType) {
        case Page_Event_OnCreate:
            return "onCreate" + " | " + "(Landroid/os/Bundle;)V";
        case Page_Event_OnResume:
            return "onResume" + " | " + "()V";
        case Page_Event_OnPostResume:
            return "onPostResume" + " | " + "()V";
        case Page_Event_OnPause:
            return "onPause" + " | " + "()V";
        case Page_Event_OnStop:
            return "onStop" + " | " + "()V";
        case Page_Event_OnDestroy:
            return "onDestroy" + " | " + "()V";
        default:
            return String.valueOf(Page_Event_Invalidate);
        }
    }

    // View的操作事件：以单击、长按为例，其他的手势，以及自定义手势都可以按照这个思路，进行劫持并插桩
    public static final int CLICK = 0;
    public static final int LONG_CLICK = 1;
    public static final int LISTVIEW_ITEM_CLICK = 2;
}

package com.lxyx.hellplugin.common
/**
 * Created by habbyge on 2019/03/30.
 *
 * 定义使用到的常量.
 */
final class HellConstant {

    private HellConstant() {
    }

    // activity事件
    public static final int Page_Event_Invalidate = -1
    public static final int Page_Event_OnCreate = 0
    public static final int Page_Event_OnNewIntent = 1
    public static final int Page_Event_OnResume = 2
    public static final int Page_Event_OnPause = 3
    public static final int Page_Event_OnStop = 4
    public static final int Page_Event_OnDestroy = 5

    // View的操作事件：以单击、长按为例，其他的手势，以及自定义手势都可以按照这个思路，进行劫持并插桩
    public static final int CLICK = 0
    public static final int LONG_CLICK = 1
    public static final int LISTVIEW_ITEM_CLICK = 2

    // 配置采用哪种注入方式，注入android/app/Activity子类
    public static final int ANDROID_APP_ACTIVITY_MODE_Inject_Method = 0 // 方案1
    public static final int ANDROID_APP_ACTIVITY_MODE_BaseActivity = 1  // 方案2
    public static final int ANDROID_APP_ACTIVITY_MODE = ANDROID_APP_ACTIVITY_MODE_BaseActivity  // 方案2
                /*ANDROID_APP_ACTIVITY_MODE_Inject_Method*/
}

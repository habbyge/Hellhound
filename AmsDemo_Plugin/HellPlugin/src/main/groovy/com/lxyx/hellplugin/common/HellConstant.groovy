package com.lxyx.hellplugin.common;

/**
 * Created by habbyge on 2019/03/30.
 *
 * 定义使用到的常量.
 */
final class HellConstant {

    private HellConstant() {
    }

    // activity事件
    public static final int ACTIVITY_EVENT_INVALIDATE = -1
    public static final int ACTIVITY_EVENT_OnCreate = 0
    public static final int ACTIVITY_EVENT_OnNewIntent = 1
    public static final int ACTIVITY_EVENT_OnResume = 2
    public static final int ACTIVITY_EVENT_OnPause = 3
    public static final int ACTIVITY_EVENT_OnStop = 4
    public static final int ACTIVITY_EVENT_OnDestroy = 5

    // fragment事件
    public static final int FRAGMENT_EVENT_INVALIDATE = -1
    public static final int FRAGMENT_EVENT_OnCreate = 0
    public static final int FRAGMENT_EVENT_OnResume = 1
    public static final int FRAGMENT_EVENT_OnPause = 2
    public static final int FRAGMENT_EVENT_OnStop = 3
    public static final int FRAGMENT_EVENT_OnDestroy = 54

    // View的操作事件：以单击、长按为例，其他的手势，以及自定义手势都可以按照这个思路，进行劫持并插桩
    public static final int CLICK = 0
    public static final int LONG_CLICK = 1
    public static final int LISTVIEW_ITEM_CLICK = 2
}
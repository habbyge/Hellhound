package com.lxyx.helllib.msgqueue;

/**
 * Created by habbyge on 2019/4/13.
 */
public final class HellMessageConstant {
    private HellMessageConstant() {
    }

    // view
    public static final int VIEW_EVENT_CLICK = 100;
    public static final int VIEW_EVENT_ITEM_CLICK = 101;
    public static final int VIEW_EVENT_LONG_CLICK = 102;

    // activity
    public static final int ACTIVITY_EVENT_OnCreate = 200;
    public static final int ACTIVITY_EVENT_OnNewIntent = 201;
    public static final int ACTIVITY_EVENT_OnResume = 202;
    public static final int ACTIVITY_EVENT_OnPause = 203;
    public static final int ACTIVITY_EVENT_OnStop = 204;
    public static final int ACTIVITY_EVENT_OnDestroy = 205;

    // fragment
    public static final int FRAGMENT_EVENT_OnCreate = 300;
    public static final int FRAGMENT_EVENT_OnResume = 301;
    public static final int FRAGMENT_EVENT_OnPause = 302;
    public static final int FRAGMENT_EVENT_OnStop = 303;
    public static final int FRAGMENT_EVENT_OnDestroy = 304;

    // activity指令
    public static final int START_ACTIVITY = 400;
    public static final int FINISH = 401;
    public static final int MOVE_TASK_TO_BACK = 402;
}

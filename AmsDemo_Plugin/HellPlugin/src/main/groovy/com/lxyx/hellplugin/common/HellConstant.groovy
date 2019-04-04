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

//    class HellPageMethodConstant {
//        String className = null
//        String methodName = null
//        String methodDesc = null
//
//        HellPageMethodConstant() {
//        }
//
//        HellPageMethodConstant(String className, String methodName, String methodDesc) {
//            this.className = className
//            this.methodName = methodName
//            this.methodDesc = methodDesc
//        }
//    }
//
//    private static final List<HellPageMethodConstant> gPageMethodState = new ArrayList<>()
//
////    static void initActivityInjectMethod() {
//    static {
//        println('HellConstant.initActivityInjectMethod()')
//
//        gPageMethodState.add(new HellPageMethodConstant('android/app/Activity', 'onCreate', '(Landroid/os/Bundle;)V'))
//        gPageMethodState.add(new HellPageMethodConstant('android/app/Activity', 'onResume', '()V'))
//        gPageMethodState.add(new HellPageMethodConstant('android/app/Activity', 'onPause', '()V'))
//        gPageMethodState.add(new HellPageMethodConstant('android/app/Activity', 'onStop', '()V'))
//        gPageMethodState.add(new HellPageMethodConstant('android/app/Activity', 'onDestroy', '()V'))
//    }
}

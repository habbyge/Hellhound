package com.lxyx.helllib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

/**
 * Created by habbyge 2019/3/6.
 */
public final class HellViewMonitor {
    private static final String TAG = "HellViewMonitor";

    private static HellViewMonitor sInstance;

    public static HellViewMonitor getInstance() {
        if (sInstance == null) {
            synchronized (HellViewMonitor.class) {
                if (sInstance == null) {
                    sInstance = new HellViewMonitor();
                }
            }
        }
        return sInstance;
    }

    private HellViewMonitor() {
    }

    public void callListenerBefore(View view, int eventType, Object params) {
        mListener.onClickBefore(view, eventType, params);
    }

    public void callListenerAfter(View view, int eventType, Object params) {
        mListener.onClickAfter(view, eventType, params);
    }

//    public static void callListenerStatic(View view, int eventType, Object params) {
//        getInstance().callListenerBefore(view, eventType, params);
//    }

    private final IHellOnClickListener mListener = new IHellOnClickListener() {

        @Override
        public void onClickBefore(View view, int eventType, Object params) {
            if (view == null) {
                return;
            }

            StringBuilder showTextSb = new StringBuilder("点击之前，注入：");

            String viewName = view.getClass().getName();
            System.out.println(TAG + ", view = " + viewName);
            showTextSb.append(viewName);

            System.out.println(TAG + ", eventType = " + eventType);
            showTextSb.append("|").append(eventType);

            if (params != null) {
                System.out.println(TAG + ", params = " + params);
                showTextSb.append("|").append(params);
            }

//            Toast toast = Toast.makeText(view.getContext(), showTextSb.toString(), Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
//            toast.show();

            view.setBackgroundResource(android.R.color.holo_green_dark);

            System.out.println("HABBYGE-MALI, onClickBefore: " + showTextSb.toString());

            // 这里从当前View开始，向上遍历，获取View树
            String viewId = getViewId(view);
            if (viewId == null || viewId.isEmpty()) {
                return;
            }
            System.out.println("habbyge-mali, onClickBefore, viewId = " + viewId);
        }

        @Override
        public void onClickAfter(View view, int eventType, Object params) {
            if (view == null) {
                return;
            }

            StringBuilder showTextSb = new StringBuilder("点击之后，注入：");

            String viewName = view.getClass().getName();
            System.out.println(TAG + ", view = " + viewName);
            showTextSb.append(viewName);

            System.out.println(TAG + ", eventType = " + eventType);
            showTextSb.append("|").append(eventType);

            if (params != null) {
                System.out.println(TAG + ", params = " + params);
                showTextSb.append("|").append(params);
            }

            Toast toast = Toast.makeText(view.getContext(), showTextSb.toString(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();

//            view.setBackgroundResource(android.R.color.holo_blue_bright);

            System.out.println("HABBYGE-MALI, onClickAfter: " + showTextSb.toString());
        }
    };

    /**
     * @return 返回的数据格式是：
     *      所属于的Activity:viewPaths(以|分割，每个控件包括控件类名和在其父类的位置，
     *      格式是: viewPath[n]):resourceid
     */
    @SuppressLint("PrivateApi")
    private static String getViewId(View view) {
        if (view == null) {
            return null;
        }

        Class<?> viewRootClass;
        Class<?> decorViewClass;
        try {
            viewRootClass = Class.forName("android.view.ViewRootImpl");
            decorViewClass = Class.forName("com.android.internal.policy.DecorView");
        } catch (Exception e) {
            return null;
        }
        String viewId = view.getClass().getSimpleName();
        ViewParent viewParent;
        while ((viewParent = view.getParent()) != null
                && !viewRootClass.isInstance(viewParent)
                && !decorViewClass.isInstance(viewParent)) {

            //noinspection StringConcatenationInLoop
            viewId += "|" + viewParent.getClass().getSimpleName();

            view = (ViewGroup) viewParent;
        }
        return viewId;
    }

    /**
     * @param eventType 这里太懒，事件编号分别对应：create-0,resume-1,pause-2,stop-3,destroy-4
     */
    public void callActivityListener(Activity activity, int eventType) {
        switch (eventType) {
        case 0:
            mActivityListener.onCreate(activity);
            break;
        case 1:
            mActivityListener.onResume(activity);
            break;
        case 2:
            mActivityListener.onPause(activity);
            break;
        case 3:
            mActivityListener.onStop(activity);
            break;
        case 4:
            mActivityListener.onDestroy(activity);
            break;
        }
    }

    private final IHellOnActivityListener mActivityListener = new IHellOnActivityListener() {
        @Override
        public void onCreate(Activity activity) {
            System.out.println("HABBYGE-MALI, mActivityListener, onCreate");
        }

        @Override
        public void onResume(Activity activity) {
            System.out.println("HABBYGE-MALI, mActivityListener, onResume");
        }

        @Override
        public void onPause(Activity activity) {
            System.out.println("HABBYGE-MALI, mActivityListener, onPause");
        }

        @Override
        public void onStop(Activity activity) {
            System.out.println("HABBYGE-MALI, mActivityListener, onStop");
        }

        @Override
        public void onDestroy(Activity activity) {
            System.out.println("HABBYGE-MALI, mActivityListener, onDestroy");
        }
    };
}

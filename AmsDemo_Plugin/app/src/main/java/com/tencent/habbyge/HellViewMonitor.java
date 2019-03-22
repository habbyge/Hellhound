package com.tencent.habbyge;

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

            view.setBackgroundResource(android.R.color.holo_red_light);

            System.out.println("HABBYGE-MALI, onClickBefore: " + showTextSb.toString());

// TODO: 2019-03-22 *************************** 待验证 ******************************** Begin
            // TODO: 2019-03-21 这里从当前View开始，向上遍历，获取View树
            View parentView = null;
            ViewParent parent;
            do {
                parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    parentView = (ViewGroup) parent;
                    System.out.println("HABBYGE-MALI, parentView: " + parentView.getClass().getName());
                }
            } while (parentView != null); // todo 这里的条件还需要加入ViewRootImpl
// TODO: 2019-03-22 *************************** 待验证 ********************************  End
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
}

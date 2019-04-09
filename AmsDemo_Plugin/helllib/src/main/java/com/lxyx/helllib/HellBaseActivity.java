package com.lxyx.helllib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by habbyge on 2019/4/8.
 * 用于【方案2】：实现编译期替换那些直接继承android/app/Activity的子类。替换方式1中注入方法的方式，方案2更加的友好和更加的无埋点.
 */
public class HellBaseActivity extends Activity {
    // 用于【方案2】，在字节码中替换掉superName，如果是android/app/Activity，替换之 ！！！
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("HABBYGE-MALI, HellBaseActivity: onCreate");
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnCreate);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("HABBYGE-MALI, HellBaseActivity: onNewIntent");
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnNewIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("HABBYGE-MALI, HellBaseActivity: onResume");
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnResume);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        System.out.println("HABBYGE-MALI, HellBaseActivity: onPostResume");
//        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_On);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnPause);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("HABBYGE-MALI, HellBaseActivity: onStop");
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnStop);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("HABBYGE-MALI, HellBaseActivity: onDestroy");
        HellMonitor.getInstance().callActivityListener(this, HellConstant.Page_Event_OnDestroy);
    }
}

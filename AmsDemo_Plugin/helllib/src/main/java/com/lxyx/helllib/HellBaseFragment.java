package com.lxyx.helllib;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by habbyge on 2019/4/12.
 */
public class HellBaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("HABBYGE-MALI, HellBaseFragment: onCreate");
        HellMonitor.getInstance().callbackFragment(this, HellConstant.Page_Event_OnCreate);
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("HABBYGE-MALI, HellBaseFragment: onResume");
        HellMonitor.getInstance().callbackFragment(this, HellConstant.Page_Event_OnResume);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("HABBYGE-MALI, HellBaseFragment: onPause");
        HellMonitor.getInstance().callbackFragment(this, HellConstant.Page_Event_OnPause);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("HABBYGE-MALI, HellBaseFragment: onStop");
        HellMonitor.getInstance().callbackFragment(this, HellConstant.Page_Event_OnStop);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("HABBYGE-MALI, HellBaseFragment: onDestroy");
        HellMonitor.getInstance().callbackFragment(this, HellConstant.Page_Event_OnDestroy);
    }
}

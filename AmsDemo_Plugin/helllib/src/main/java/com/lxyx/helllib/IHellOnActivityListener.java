package com.lxyx.helllib;

import android.app.Activity;

/**
 * Created by habbyge 2019/03/24.
 */
public interface IHellOnActivityListener {
    void onCreate(Activity activity);

    void onResume(Activity activity);

    void onPause(Activity activity);

    void onStop(Activity activity);

    void onDestroy(Activity activity);
}

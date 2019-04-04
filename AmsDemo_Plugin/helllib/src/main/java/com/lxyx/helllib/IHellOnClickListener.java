package com.lxyx.helllib;

import android.view.View;

/**
 * Created by habbyge on 2019/3/6.
 */
public interface IHellOnClickListener {
    void onClickBefore(int clickType, View view);
    void onClickAfter(int clickType, View view);
}

package com.lxyx.helllib;

import android.view.View;

/**Ï
 * Created by habbyge 2019/3/6.
 */
public interface IHellOnClickListener {
    void onClickBefore(int clickType, View view);
    void onClickAfter(int clickType, View view);
}

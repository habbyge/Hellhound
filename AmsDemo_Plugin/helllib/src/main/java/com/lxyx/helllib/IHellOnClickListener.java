package com.lxyx.helllib;

import android.view.View;

/**Ï
 * Created by habbyge 2019/3/6.
 */
public interface IHellOnClickListener {
    void onClickBefore(View view, int eventType, Object params);
    void onClickAfter(View view, int eventType, Object params);
}

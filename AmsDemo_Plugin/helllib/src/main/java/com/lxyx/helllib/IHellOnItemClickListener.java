package com.lxyx.helllib;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by habbyge 2019/3/29.
 */
public interface IHellOnItemClickListener {
    void onItemClickBefore(AdapterView<?> parent, View view, int position, long id);
    void onItemClickAfter(AdapterView<?> parent, View view, int position, long id);
}

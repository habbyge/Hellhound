package com.lxyx.helllib;

import android.app.Activity;
import android.content.Context;

public class HellMonitorUtil {
    private HellMonitorUtil() {
    }

    public static boolean isActivityOrContentType(Object caller) {
        return  (caller instanceof Activity) || (caller instanceof Context);
    }
}

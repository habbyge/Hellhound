package com.lxyx.hellplugin.common;

import java.util.HashMap;
import java.util.Map;

public class HellPageMethodConstant {
    private String className = null;
    private String methodName = null;
    private String methodDesc = null;

    public HellPageMethodConstant() {
    }

    private HellPageMethodConstant(String className, String methodName, String methodDesc) {
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    // 记录系统的android.jar中的需要注入的目标方法的状态，标识是否已经注入过插桩，避免遗漏和重复
    private static final Map<HellPageMethodConstant, Boolean> gPageMethodStateMap = new HashMap<>();

    public static void setMethodState(String className, String name, String desc) {
        gPageMethodStateMap.put(new HellPageMethodConstant(className, name, desc), true);
    }

    public static Boolean getMethodState(String className, String name, String desc) {
        HellPageMethodConstant key;
        for (Map.Entry<HellPageMethodConstant, Boolean> entry : gPageMethodStateMap.entrySet()) {
            key = entry.getKey();
            if (key.className.equals(className) && key.methodName.equals(name) && key.methodDesc.equals(desc)) {
                return entry.getValue();
            }
        }
        return false;
    }

    public static void reset() {
        gPageMethodStateMap.clear();
    }
}

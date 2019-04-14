package com.lxyx.helllib.msgqueue;

public class HellMessage {
    public int eventType;
    public Object arg;

    public HellMessage(int eventType, Object arg) {
        this.eventType = eventType;
        this.arg = arg;
    }
}

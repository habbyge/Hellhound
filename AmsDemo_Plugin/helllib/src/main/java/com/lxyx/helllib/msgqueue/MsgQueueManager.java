package com.lxyx.helllib.msgqueue;

/**
 * Created by habbhyge on 2019/4/10.
 * 目的：为了避免业务逻辑影响原有逻辑的执行损耗，例如影响主UI线程的执行时效，把callback回的事件和数据add到一个子线程中去处理即可.
 * 消息队列，用于接收监控核心模块的回调事件，并获取参数，实现策略是：独立子线程用于接收callback消息体，
 * 采用生产者-消费者模型，注意这里一定要保证时序。
 * 监控核心模块只负责在合适的时机，向上传递出基本的数据即可(页面对象、时间戳、参数等)，重型的业务逻辑在这个消息队列中执行。
 */
public final class MsgQueueManager {
    private static final String TAG = "MsgQueueManager";

    private static MsgQueueManager sInstance;
    private MsgQueue msgQueue;

    private MsgQueueManager() {
        msgQueue = new MsgQueue();
    }

    public static MsgQueueManager getInstance() {
        if (sInstance == null) {
            synchronized (MsgQueueManager.class) {
                if (sInstance == null) {
                    sInstance = new MsgQueueManager();
                }
            }
        }
        return sInstance;
    }

    public void sendMsg(HellMsg msg) {
        msgQueue.sendMsg(msg);
    }
}

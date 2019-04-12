package com.lxyx.helllib.msgqueue;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by habbhyge on 2019/4/10.
 */
// TODO: 2019-04-11 支持多进程共享 ？？？？？？？？？？？？？？？？？？暂时不支持
final class MsgQueue {

    // TODO: 2019-04-10 核心是一个阻塞式队列：有序、阻塞、生产者消费者模型。
    private BlockingQueue<HellMsg> mMsgQueue;
    private Handler mHandler;

    private static final int HELL_MSG_SEND = 0;
    private static final int HELL_MSG_FETCH = 1;

    MsgQueue() {
        mMsgQueue = new LinkedBlockingQueue<>(16);
        HandlerThread productorHandlerThread = new HandlerThread("hell_msgqueue_send_thread");
        productorHandlerThread.start();
        mHandler = new HellHandler(productorHandlerThread.getLooper());
    }

    void sendMsg(HellMsg msg) {
        Message sendMsg = new Message();
        sendMsg.what = HELL_MSG_SEND;
        sendMsg.obj = msg;
        mHandler.sendMessage(sendMsg);
    }

    private final class HellHandler extends Handler {
        HellHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HELL_MSG_SEND:
                try {
                    mMsgQueue.put((HellMsg) msg.obj);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                break;

            case HELL_MSG_FETCH:
                try {
                    HellMsg hellMsg = mMsgQueue.take();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}

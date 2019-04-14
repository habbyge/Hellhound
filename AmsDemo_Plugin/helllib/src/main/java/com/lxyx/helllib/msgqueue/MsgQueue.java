package com.lxyx.helllib.msgqueue;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by habbhyge on 2019/4/10.
 */
// TODO: 2019-04-11 支持多进程共享 ？？？？？？？？？？？？？？？？？？暂时不支持
final class MsgQueue {

    // 核心是一个阻塞式队列：有序、阻塞、生产者消费者模型。
    private BlockingQueue<HellMessage> mMsgQueue;
    private Handler mMsgHandler;

    private static final int HELL_MSG_SEND = 0;

    MsgQueue() {
        mMsgQueue = new ArrayBlockingQueue<>(5);
        HandlerThread handlerThread = new HandlerThread("hell_msgq_thread");
        handlerThread.start();
        mMsgHandler = new MsgHandler(handlerThread.getLooper());

        readyToReceiveMsg(); // 准备好开始接收消息
    }

    void sendMessage(HellMessage hellMessage) {
        if (hellMessage == null) {
            return;
        }

        Message message = new Message();
        message.what = HELL_MSG_SEND;
        message.obj = hellMessage;
        mMsgHandler.sendMessage(message);
    }

    private void readyToReceiveMsg() {
        new Thread(new ReceiverRunnable()).start();
    }

    private final class MsgHandler extends Handler {
        MsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what != HELL_MSG_SEND) {
                return;
            }
            if (!(message.obj instanceof HellMessage)) {
                return;
            }
            HellMessage hellMessage = (HellMessage) message.obj;
            try {
                mMsgQueue.put((HellMessage) message.obj);
                System.out.println("HABBYGE-MALI, HELL_MSG_SEND: " + hellMessage.eventType + " | " + hellMessage.arg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private final class ReceiverRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    HellMessage hellMessage = mMsgQueue.take();
                    System.out.println("HABBYGE-MALI, ReceiverHandler: " +
                            hellMessage.eventType + " | " + hellMessage.arg);

                    // TODO: 2019-04-13 这里callback回消息到业务层
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}

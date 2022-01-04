package net.cqyc.sample.clink.core;

import java.io.Closeable;

/**
 * @Description: 接收的数据调度封装
 * 把一份或者多份ioArgs组合成一份packet
 * @author: cqyc
 * @date 2021/12/31
 */
public interface ReceiveDispatcher extends Closeable {

    void start();

    void stop();

    interface ReceivePacketCallback{
        void onReceivePacketCompleted(ReceivePacket packet);
    }


}

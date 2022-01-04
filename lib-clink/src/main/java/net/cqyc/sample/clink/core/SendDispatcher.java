package net.cqyc.sample.clink.core;

import java.io.Closeable;

/**
 * @Description: 发送数据的调度者，
 * 缓存所有需要发送的数据、通过队列对数据进行发送
 * 并且在发送数据时，实现对数据的基本包装
 * @author: cqyc
 * @date 2021/12/31
 */
public interface SendDispatcher extends Closeable {

    /**
     * 发送一份数据
     * @param packet
     */
    void send(SendPacket packet);

    /**
     * 取消发送
     */
    void cancel(SendPacket packet);
}

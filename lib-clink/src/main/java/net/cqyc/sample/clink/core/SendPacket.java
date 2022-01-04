package net.cqyc.sample.clink.core;

/**
 * @Description: 发送包的一个定义
 * @author: cqyc
 * @date 2021/12/31
 */
public abstract class SendPacket extends Packet{

    private boolean isCanceled;

    public abstract byte[] bytes();

    public boolean isCanceled() {
        return false;
    }
}

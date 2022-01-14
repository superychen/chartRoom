package net.cqyc.sample.clink.core;

import java.io.InputStream;

/**
 * @Description: 发送包的一个定义
 * @author: cqyc
 * @date 2021/12/31
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T>{



    private boolean isCanceled;

    public boolean isCanceled() {
        return false;
    }


}

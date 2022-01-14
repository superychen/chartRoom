package net.cqyc.sample.clink.core;

import java.io.OutputStream;

/**
 * @Description: 接收包的定义
 * @author: cqyc
 * @date 2021/12/31
 */
public abstract class ReceivePacket<T extends OutputStream> extends Packet<T>{

//     public abstract void save(byte[] bytes, int count );
}

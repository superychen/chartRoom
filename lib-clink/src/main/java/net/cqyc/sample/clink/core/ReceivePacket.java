package net.cqyc.sample.clink.core;

/**
 * @Description: 接收包的定义
 * @author: cqyc
 * @date 2021/12/31
 */
public abstract class ReceivePacket extends Packet{

     public abstract void save(byte[] bytes, int count );

}

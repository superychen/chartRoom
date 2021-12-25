package net.cqyc.sample.clink.core;

import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public class Connector {
    private UUID key = UUID.randomUUID();

    private SocketChannel channel;

    private Sender sender;

    private Receiver receiver;

    public void setup(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }
}

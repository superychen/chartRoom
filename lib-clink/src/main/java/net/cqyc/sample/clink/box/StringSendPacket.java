package net.cqyc.sample.clink.box;

import net.cqyc.sample.clink.core.SendPacket;

import java.io.ByteArrayInputStream;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class StringSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        this.length = bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}

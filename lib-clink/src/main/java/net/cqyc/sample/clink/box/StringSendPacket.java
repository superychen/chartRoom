package net.cqyc.sample.clink.box;

import net.cqyc.sample.clink.core.SendPacket;

import java.io.IOException;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class StringSendPacket extends SendPacket {

    private final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        this.length = bytes.length;
    }

    @Override
    public byte[] bytes() {
        return bytes;
    }

    @Override
    public void close() throws IOException {

    }
}

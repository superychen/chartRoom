package net.cqyc.sample.clink.box;

import net.cqyc.sample.clink.core.ReceivePacket;

import java.io.IOException;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class StringReceivePacket extends ReceivePacket {

    private byte[] buffer;
    private int position;

    public StringReceivePacket(int len) {
        buffer = new byte[len];
        length = length;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, position, count);
        position += count;
    }

    public String string() {
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}

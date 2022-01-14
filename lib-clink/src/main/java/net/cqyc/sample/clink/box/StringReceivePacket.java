package net.cqyc.sample.clink.box;

import net.cqyc.sample.clink.core.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {

//    private byte[] buffer;
//    private int position;

    private String string;

    public StringReceivePacket(int len) {
//        buffer = new byte[len];
        length = len;
    }

//    @Override
//    public void save(byte[] bytes, int count) {
//        System.arraycopy(bytes, 0, buffer, position, count);
//        position += count;
//    }

    public String string() {
        return string;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        string = new String(stream.toByteArray());

    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream( (int) length);
    }

}

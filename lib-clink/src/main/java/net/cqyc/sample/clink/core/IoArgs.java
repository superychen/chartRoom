package net.cqyc.sample.clink.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public class IoArgs {
    private int limit = 256;
    private byte[] byteBuffer = new byte[256];

    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * 从bytes读取数据
     * @param bytes
     * @param offset
     * @return
     */
    public int readFrom(byte[] bytes, int offset) {
        //拿到当前可以容纳的一个大小
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        //返回本次操作了多少个size
        return size;
    }

    /**
     * 写入数据到bytes中
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes, int offset) {
        //拿到当前可以容纳的一个大小
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        //返回本次操作了多 少个size
        return size;
    }

    /**
     * 从socketChannel读取数据
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();

        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if(len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        finishWriting();

        return bytesProduced;
    }

    /**
     * 写数据到socketChannel
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if(len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced ;
    }

    /**
     * 开始写入数据到ioArgs
     */
    public void startWriting() {
        buffer.clear();
        //定义容纳区间
        buffer.limit(limit);
    }

    /**
     * 写完数据后调用
     */
    public void finishWriting() {
        //反转
        buffer.flip();
    }

    /**
     * 设置单次写操作的容纳区间
     * @param limit 区间大小
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

//    public String bufferString() {
//        //丢弃换行符
//        return new String(byteBuffer, 0, buffer.position() - 1);
//    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}

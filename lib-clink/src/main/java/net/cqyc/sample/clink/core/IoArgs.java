package net.cqyc.sample.clink.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public class IoArgs {
    private int limit = 5;
    private ByteBuffer buffer = ByteBuffer.allocate(limit);

    /**
     * 从bytes读取数据
     * @return
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
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
     * 写入数据到bytes中
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
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
        startWriting();
        buffer.putInt(total);
        finishWriting();
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

    /**
     * IoArgs 提供者、处理者：数据生产和消费者
     */
    public interface IoArgsEventProcessor {
        /**
         * 提供一份可消费的ioArgs
         * @return
         */
        IoArgs provideIoArgs();

        /**
         * 消费失败时回调
         * @param args
         * @param e
         */
        void onConsumeFailed(IoArgs args, Exception e);

        /**
         * 消费成功
         * @param args
         */
        void onConsumeCompleted(IoArgs args);
    }

}

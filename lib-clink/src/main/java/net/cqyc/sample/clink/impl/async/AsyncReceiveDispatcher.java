package net.cqyc.sample.clink.impl.async;

import net.cqyc.sample.clink.box.StringReceivePacket;
import net.cqyc.sample.clink.core.IoArgs;
import net.cqyc.sample.clink.core.ReceiveDispatcher;
import net.cqyc.sample.clink.core.ReceivePacket;
import net.cqyc.sample.clink.core.Receiver;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description:
 * @author: cqyc
 * @date 2022/1/1
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcessor {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket<?> packetTemp;

    private WritableByteChannel packetChannel;
    private long total;
    private int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
        this.callback = callback;
    }


    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if(isClosed.compareAndSet(false, true)) {
            completePacket(false);
        }
    }


    /**
     * 解析数据到packet
     */
    private void assemblePacket(IoArgs args) {
        if(packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);

            packetChannel = Channels.newChannel(packetTemp.open());

            total = length;
            position = 0;
        }
        int count = 0;
        try {
            count = args.writeTo(packetChannel);
            position += count;
            //检查是否已完成一份packet接收
            if(position == total) {
                completePacket(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            completePacket(false);
        }
    }

    /**
     * 完成数据操作
     */
    private void completePacket(boolean isSucceed) {
        ReceivePacket packet = this.packetTemp;

        CloseUtils.close(packet);
        packetTemp = null;

        WritableByteChannel channel = this.packetChannel;
        CloseUtils.close(channel);
        packetChannel = null;

        if(packet != null) {
            callback.onReceivePacketCompleted(packet);
        }
    }


    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int receiveSize;
        if(packetTemp == null) {
            receiveSize = 4;
        } else {
            receiveSize = (int) Math.min(total - position, args.capacity());
        }
        //设置本次接收数据大小
        args.limit(receiveSize);
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //解析数据
        assemblePacket(args);
        //接收下一次数据
        registerReceive();
    }
}

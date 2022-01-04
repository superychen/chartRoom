package net.cqyc.sample.clink.impl.async;

import net.cqyc.sample.clink.box.StringReceivePacket;
import net.cqyc.sample.clink.core.IoArgs;
import net.cqyc.sample.clink.core.ReceiveDispatcher;
import net.cqyc.sample.clink.core.ReceivePacket;
import net.cqyc.sample.clink.core.Receiver;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description:
 * @author: cqyc
 * @date 2022/1/1
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket packetTemp;
    private byte[] buffer;
    private int total;
    private int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(ioArgsEventListener);
        this.callback = callback;
    }


    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.receiveAsync(ioArgs);
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
            ReceivePacket packet = this.packetTemp;
            if(packet != null) {
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }

    public final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {
            int receiveSize;
            if(packetTemp == null) {
                receiveSize = 4;
            } else {
                receiveSize = Math.min(total - position, args.capacity());
            }
            //设置本次接收数据大小
            args.limit(receiveSize);
        }

        @Override
        public void onCompleted(IoArgs args) {
            assemblePacket(args);
            //继续接收下一条消息
            registerReceive();
        }

    };

    /**
     * 解析数据到packet
     */
    private void assemblePacket(IoArgs args) {
        if(packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);
            buffer = new byte[length];
            total = length;
            position = 0;
        }
        int count = args.writeTo(buffer, 0);
        if(count > 0) {
            packetTemp.save(buffer, count);
            position += count;
            //检查是否已完成一份packet接收
            if(position == total) {
                completePacket();
                packetTemp = null;
            }
        }
    }

    /**
     * 完成数据操作
     */
    private void completePacket() {
        ReceivePacket packet = this.packetTemp;
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }


}

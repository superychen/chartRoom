package net.cqyc.sample.clink.impl.async;

import net.cqyc.sample.clink.core.IoArgs;
import net.cqyc.sample.clink.core.SendDispatcher;
import net.cqyc.sample.clink.core.SendPacket;
import net.cqyc.sample.clink.core.Sender;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor{

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);


    private IoArgs ioArgs = new IoArgs();
    private SendPacket<?> packetTemp;

    //当前发送的packet的大小，以及进度
    private ReadableByteChannel packetChannel;
    private long total;
    private int position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendListener(this);
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if(isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if(packet != null && packet.isCanceled()) {
            //已取消，不用发送
            return takePacket();
        }
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = packetTemp;
        if(temp != null) {
            CloseUtils.close(temp);
        }
        SendPacket packet = packetTemp = takePacket();
        if(packet == null) {
            isSending.set(false);
            return;
        }
        total = packet.length();
        position = 0;

        sendCurrentPacket();
    }

    //真实发送packet信息
    private void sendCurrentPacket() {
        if(position >= total) {
            completePacket(position == total);
            sendNextPacket();
            return;
        }
        //发送数据
        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    /**
     * 完成packet发送
     * @param isSucceed 是否成功
     */
    private void completePacket(boolean isSucceed) {
        SendPacket packet = this.packetTemp;
        if(packet == null) {
            return;
        }
        CloseUtils.close(packet);
        CloseUtils.close(packetChannel);

        packetTemp = null;
        packetChannel = null;
        total = 0;
        position = 0;
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }



    @Override
    public void cancel(SendPacket packet) {

    }

    @Override
    public void close() throws IOException {
        if(isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            //异常关闭导致关闭
            completePacket(false);
        }
    }

    @Override
    public IoArgs provideIoArgs()  {
        IoArgs args = ioArgs;
        if(packetChannel == null) {
            packetChannel = Channels.newChannel(packetTemp.open());
            args.limit(4);
            args.writeLength((int) packetTemp.length());
        } else {
            args.limit((int) Math.min(args.capacity(), total - position));

            try {
                int count = args.readFrom(packetChannel);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        //继续发送当前包
        sendCurrentPacket();
    }
}

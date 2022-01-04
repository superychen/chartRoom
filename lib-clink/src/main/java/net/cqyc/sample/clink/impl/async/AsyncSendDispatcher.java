package net.cqyc.sample.clink.impl.async;

import net.cqyc.sample.clink.core.IoArgs;
import net.cqyc.sample.clink.core.SendDispatcher;
import net.cqyc.sample.clink.core.SendPacket;
import net.cqyc.sample.clink.core.Sender;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class AsyncSendDispatcher implements SendDispatcher {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);


    private IoArgs ioArgs = new IoArgs();
    private SendPacket packetTemp;

    private int total;
    private int position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
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
        IoArgs args = ioArgs;
        args.startWriting();
        if(position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            //首包，需要携带长度信息
            args.writeLength(total);
        }
        byte[] bytes = packetTemp.bytes();
        int count = args.readFrom(bytes, position);
        position += count;

        //完成封装
        args.finishWriting();

        //发送数据
        try {
            sender.sendAsync(args, ioArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }


    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //继续发送当前包
            sendCurrentPacket();
        }
    };

    @Override
    public void cancel(SendPacket packet) {

    }

    @Override
    public void close() throws IOException {
        if(isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            SendPacket packet = this.packetTemp;
            if(packet != null) {
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }
}

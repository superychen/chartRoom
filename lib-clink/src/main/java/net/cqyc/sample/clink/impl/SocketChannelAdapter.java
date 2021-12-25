package net.cqyc.sample.clink.impl;

import net.cqyc.sample.clink.core.IoArgs;
import net.cqyc.sample.clink.core.IoProvider;
import net.cqyc.sample.clink.core.Receiver;
import net.cqyc.sample.clink.core.Sender;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cqyc
 * @Description: 输入和输出发送者和接收者
 * @date 2021/12/16
 */
public class SocketChannelAdapter implements Sender, Receiver, Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IoArgs.IoArgsEventListener receiveEventListener;
    private IoArgs.IoArgsEventListener sendIoEventListener;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        //设置为非阻塞
        channel.configureBlocking(false);
    }

    /**
     * 异步接收
     */
    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        receiveEventListener = listener;
        return ioProvider.registerInput(channel, inputCallback);
    }

    /**
     * 异步发送
     */
    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        sendIoEventListener = listener;
        //当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            CloseUtils.close(channel);
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs args = new IoArgs();
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveEventListener;
            if (listener != null) {
                listener.onStarted(args);
            }

            //具体的读取操作
            try {
                if (args.read(channel) > 0 && listener != null) {
                    //读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("cannot read any data!");
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }
            //todo
            sendIoEventListener.onCompleted(null);
        }
    };

    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}

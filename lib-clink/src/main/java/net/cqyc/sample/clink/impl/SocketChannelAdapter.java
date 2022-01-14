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

    private IoArgs.IoArgsEventProcessor receiveEventProcessor;
    private IoArgs.IoArgsEventProcessor sendIoEventProcessor;


    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        //设置为非阻塞
        channel.configureBlocking(false);
    }


    @Override
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) {
        receiveEventProcessor  = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcessor processor) {
        sendIoEventProcessor  = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
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
            IoArgs.IoArgsEventProcessor processor = SocketChannelAdapter.this.receiveEventProcessor ;

            IoArgs args = processor.provideIoArgs();

             //具体的读取操作
            try {
                if (args.readFrom(channel) > 0) {
                    //读取完成回调
                    processor.onConsumeCompleted( args);
                } else {
                    processor.onConsumeFailed(args, new IOException("cannot read any data!"));
                    throw new IOException("cannot read any data!");
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IoProvider.HandleOutputCallback outputCallback = new  IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = sendIoEventProcessor;
            IoArgs args = processor.provideIoArgs();

            //具体的写操作
            try {
                System.out.println("write 具体的写操作！！");
                if (args.writeTo(channel) > 0) {
                    //读取完成回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot write any data!"));
                    throw new IOException("cannot write any data!");
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }

    };


    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}

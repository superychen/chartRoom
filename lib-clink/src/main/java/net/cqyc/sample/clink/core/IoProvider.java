package net.cqyc.sample.clink.core;


import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public interface IoProvider extends Closeable {

    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    abstract class HandleInputCallback implements Runnable {
        @Override
        public void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    abstract class HandleOutputCallback implements Runnable {
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }
        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        public final <T> T getAttach() {
            return (T) attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }


}

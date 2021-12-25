package net.cqyc.sample.clink.core;

import com.sun.org.apache.bcel.internal.generic.RET;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public class IoContext implements Closeable {

    private static IoContext INSTANCE;

    private final IoProvider ioProvider;

    private IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public static IoContext get() {
        return INSTANCE;
    }
    public static StartedBoot setup() {
        return new StartedBoot();
    }



    @Override
    public void close() throws IOException {
        ioProvider.close();
    }

    public static class StartedBoot {
        private IoProvider ioProvider;

        public StartedBoot() {
        }

        public StartedBoot(IoProvider ioProvider) {
            this.ioProvider = ioProvider;
        }

        public IoContext start() {
            INSTANCE = new IoContext(ioProvider);
            return INSTANCE;
        }

    }
}

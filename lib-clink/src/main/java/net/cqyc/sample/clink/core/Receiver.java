package net.cqyc.sample.clink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2020/4/25
 */
public interface Receiver extends Closeable {

    void setReceiveListener(IoArgs.IoArgsEventProcessor processor);

    boolean postReceiveAsync() throws IOException;
}

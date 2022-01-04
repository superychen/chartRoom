package net.cqyc.sample.clink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2020/4/25
 */
public interface Receiver extends Closeable {

    void setReceiveListener(IoArgs.IoArgsEventListener listener);

    boolean receiveAsync(IoArgs args) throws IOException;
}

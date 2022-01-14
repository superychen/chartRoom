package net.cqyc.sample.clink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public interface Sender extends Closeable {

    void setSendListener(IoArgs.IoArgsEventProcessor processor);

    boolean postSendAsync() throws IOException;

}

package net.cqyc.sample.clink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2021/12/5
 */
public interface Sender extends Closeable {

    boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException;

}

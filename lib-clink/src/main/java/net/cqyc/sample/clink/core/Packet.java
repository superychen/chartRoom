package net.cqyc.sample.clink.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @Description: 公共的数据封装、提供了类型以及基本的长度的定义
 * @author: cqyc
 * @date 2021/12/31
 */
public abstract class Packet<T extends Closeable> implements Closeable {

    protected byte type;
    protected long length;

    private T stream;

    public byte type() {
        return type;
    }

    public long length() {
        return length;
    }

    public final T open(){
        if(stream == null) {
            stream = createStream();
        }
        return stream;
    }

    protected abstract T createStream();

    protected void closeStream(T stream) throws IOException {
        stream.close();
    }

    @Override
    public final void close() throws IOException {
        if(stream != null) {
            closeStream(stream);
            stream = null;
        }
    }

}

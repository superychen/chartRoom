package net.cqyc.sample.clink.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/6
 */
public class CloseUtils {

    public static void close(Closeable... closeables) {
        if(closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

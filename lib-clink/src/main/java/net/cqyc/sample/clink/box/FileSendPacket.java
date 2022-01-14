package net.cqyc.sample.clink.box;

import net.cqyc.sample.clink.core.SendPacket;

import java.io.*;

/**
 * @Description:
 * @author: cqyc
 * @date 2021/12/31
 */
public class FileSendPacket extends SendPacket<FileInputStream> {

    public FileSendPacket(File file) {
        this.length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}

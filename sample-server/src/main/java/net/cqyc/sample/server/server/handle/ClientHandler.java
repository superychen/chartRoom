package net.cqyc.sample.server.server.handle;

import net.cqyc.sample.clink.core.Connector;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/6
 */
public class ClientHandler extends Connector{

    private final ClientHandlerCallback clientHandlerCallback;
    //自身基础信息描述
    private final String clientInfo;


    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();

        System.out.println("新客户端连接：" + clientInfo);
        setup(socketChannel);
    }


    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出：" + clientInfo);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        clientHandlerCallback.onNewMessageArrived(this, str);
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    //回调
    public interface ClientHandlerCallback {
        //自身关闭通知
        void onSelfClosed(ClientHandler handler);

        //收到消息时通知
        void onNewMessageArrived(ClientHandler handler, String msg);
    }


}

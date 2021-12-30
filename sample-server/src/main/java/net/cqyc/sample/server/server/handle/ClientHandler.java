package net.cqyc.sample.server.server.handle;

import net.cqyc.sample.clink.core.Connector;
import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/6
 */
public class ClientHandler {

    private Connector connector;
    private final SocketChannel socketChannel;
//    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    //自身基础信息描述
    private final String clientInfo;


    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel;

        connector = new Connector() {
            @Override
            public void onChannelClosed(SocketChannel channel) {
                super.onChannelClosed(channel);
                exitBySelf();
            }

            @Override
            protected void onReceiveNewMessage(String str) {
                super.onReceiveNewMessage(str);
                clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str    );
            }
        };
        connector.setup(socketChannel);
//        //设置非阻塞模式
//        socketChannel.configureBlocking(false);
//
//        Selector readSelector = Selector.open();
//        //设置一个读就绪的事件
//        socketChannel.register(readSelector, SelectionKey.OP_READ);
//        this.readHandler = new ClientReadHandler(readSelector);

        Selector writeSelector = Selector.open();
        //设置一个读就绪事件
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
        this.writeHandler = new ClientWriteHandler(writeSelector);

        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void exit() {
        CloseUtils.close(connector);
        writeHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出：" + clientInfo);
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


    class ClientWriteHandler extends Thread {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;
        private final ExecutorService executorService;

        ClientWriteHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        void send(String str) {
            //如果自己已经下线，则直接返回
            if(done) {
                return;
            }
            this.executorService.execute(new WriteRunnable(str));
        }

        //发送数据
        class WriteRunnable implements Runnable {
            private final String msg;

            WriteRunnable(String msg) {
                this.msg = msg + '\n';
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }
                byteBuffer.clear();
                byteBuffer.put(msg.getBytes());
                //反转操作, 上面数据指针指向的数据的最后，如果发送的话，是从指针开始的地方依次发送，所以需要反转
                //将指针指向初始位置，然后结束位置等于之前的指针位置
                byteBuffer.flip();

                //当前是否还有 发送的数据
                while(!done && byteBuffer.hasRemaining()) {
                    try {
                        int len = socketChannel.write(byteBuffer);
                        //len = 0合法
                        if(len < 0) {
                            System.out.println("客户端已经无法发送数据");
                            ClientHandler.this.exitBySelf();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



}

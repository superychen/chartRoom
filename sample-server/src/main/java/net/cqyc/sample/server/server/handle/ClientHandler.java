package net.cqyc.sample.server.server.handle;

import net.cqyc.sample.clink.utils.CloseUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/6
 */
public class ClientHandler {

    private final SocketChannel socketChannel;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    //自身基础信息描述
    private final String clientInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socketChannel = socketChannel;
        //设置非阻塞模式
        socketChannel.configureBlocking(false);

        Selector readSelector = Selector.open();
        //设置一个读就绪的事件
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        this.readHandler = new ClientReadHandler(readSelector);

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
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出：" + clientInfo);
    }

    public void readToPrint() {
        //启动线程
        readHandler.start();
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

    class ClientReadHandler extends Thread {

        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;

        ClientReadHandler(Selector selector) {
            this.selector = selector;
            byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();

            try {
                do {
                    //客户端拿到一条数据
                    if(selector.select() == 0) {
                        if(done) {
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if(done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        //如果当前可读
                        if(key.isReadable()) {
                            //拿到注册的channel，为socketChannel
                            SocketChannel client = (SocketChannel) key.channel();
                            //清空操作
                            byteBuffer.clear();
                            //将数据读到byteBuffer中，并返回读到的多少字节
                            int read = client.read(byteBuffer);
                            if(read > 0) {
                                //按照行输出，带有一个结束符，会把行结束符读取，所以需要减一，丢弃换行符
                                String str = new String(byteBuffer.array(), 0, byteBuffer.position() - 1);
                                //通知到tcpServer
                                clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str);
                            } else {
                                System.out.println("客户端已无法读取数据！");
                                //退出当前客户端
                                ClientHandler.this.exitBySelf();
                                break;
                            }
                        }
                    }
                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                // 连接关闭
                CloseUtils.close(selector);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
        }
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

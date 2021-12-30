package net.cqyc.sample.server.server;


import net.cqyc.sample.clink.utils.CloseUtils;
import net.cqyc.sample.server.server.handle.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cqyc
 * @Description:
 * @date 2021/8/24
 */
public class TCPServer implements ClientHandler.ClientHandlerCallback{

    private final int port;
    private ClientListener listener;
//    private List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>()); 只是对内部方法操作上来说是线程安全的，在添加和操作时，同时遍历list也会存在安全问题
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;
    private Selector selector;
    private ServerSocketChannel server;

    public TCPServer(int port) {
        this.port = port;
        //转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            selector = Selector.open();
            //启动一个通道
            this.server = ServerSocketChannel.open();
            //配置他为非阻塞状态
            server.configureBlocking(false);
            //绑定本地端口
            server.socket().bind(new InetSocketAddress(port));
            //注册一个事件, 注册一个当有客户端到达的监听事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器信息： " + server.getLocalAddress().toString());
            //启动客户端的监听
            ClientListener listener = this.listener = new ClientListener();
            listener.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }
        CloseUtils.close(server);
        CloseUtils.close(selector);
        //保证顺序退出
        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }
        //停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(final ClientHandler handler, final String msg) {
        //异步提交转发任务
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if(clientHandler.equals(handler)) {
                        //跳过自己
                        continue;
                    }
                    
                    //对其他人就发送这条消息
                    clientHandler.send(msg);
                }
            }
        });
    }

    private class ClientListener extends Thread {
        private boolean done = false;

        @Override
        public void run() {
            super.run();

            Selector selector = TCPServer.this.selector;
            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    //默认让他永远阻塞
                    if(selector.select() == 0) {
                        if(done) {
                            break;
                        }
                        continue;
                    }
                    System.out.println("我试试这里能不能走");
                    //拿当前就绪的事件
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if(done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        //检查当前key的状态是否是我们关注的
                        //客户端到达状态
                        if(key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            //此时这个accept一定是有数据返回的，当有客户端连接的时候，才通知到这里
                            //非阻塞状态拿到客户端连接
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            try {
                                ClientHandler clientHandler = new ClientHandler(socketChannel,TCPServer.this);
                                synchronized (TCPServer.this) {
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("客户端连接异常：" + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            //唤醒当前阻塞
            selector.wakeup();
        }
    }




}

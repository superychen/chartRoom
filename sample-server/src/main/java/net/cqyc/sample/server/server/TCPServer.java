package net.cqyc.sample.server.server;


import net.cqyc.sample.server.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    private ClientListener mListener;
//    private List<ClientHandler> clientHandlerList = Collections.synchronizedList(new ArrayList<>()); 只是对内部方法操作上来说是线程安全的，在添加和操作时，同时遍历list也会存在安全问题
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;

    public TCPServer(int port) {
        this.port = port;
        //转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }
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
        //打印到屏幕上
        System.out.println("Received-" + handler.getClientInfo() + ":" + msg);

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
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息： " + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {
            super.run();

            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                // 客户端构建异步线程
                ClientHandler clientHandler = null;
                try {
                    clientHandler = new ClientHandler(client,TCPServer.this);
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }

            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}

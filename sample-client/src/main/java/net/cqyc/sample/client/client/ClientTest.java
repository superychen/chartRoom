package net.cqyc.sample.client.client;

import net.cqyc.sample.client.client.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/7
 */
public class ClientTest {

    private static boolean done;

    public static void main(String[] args) throws IOException {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if(info == null) {
            return;
        }

        //当前连接数量
        int size = 0;
        List<TCPClient> tcpClients = new ArrayList<>();
        for (int i = 0; i <1000; i++) {
            try {
                TCPClient tcpClient = TCPClient.startWith(info);
                if(tcpClient == null) {
                    System.out.println("连接异常");
                    continue;
                }
                tcpClients.add(tcpClient);
                System.out.println("连接成功" + (++size));
            } catch (IOException e) {
                System.out.println("连接异常");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.in.read();

        Runnable runnable = () -> {
            while (!done) {
                for (TCPClient tcpClient : tcpClients) {
                    tcpClient.send("Hello~~~");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();

        //等待线程完成
        done = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //客户端结束操作
        for (TCPClient tcpClient : tcpClients) {
            tcpClient.exit();
        }
    }

}

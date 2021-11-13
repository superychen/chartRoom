package net.cqyc.sample.client.client;


import net.cqyc.sample.client.client.bean.ServerInfo;

import java.io.*;
import java.net.Socket;


/**
 * @author cqyc
 * @Description:
 * @date 2021/8/24
 */
public class Client {

    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if(info != null) {
            TCPClient tcpClient = null;
            try {
                tcpClient = TCPClient.startWith(info);
                if(tcpClient == null) {
                    return;
                }
                write(tcpClient);
            }catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    public static void write(TCPClient tcpClient) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            tcpClient.send(str);

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);

    }

}

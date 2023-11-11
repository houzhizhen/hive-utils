package com.baidu.hive.jdbc;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.thrift.TException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerBacklogTest {

    public static void main(String[] args) throws HiveException, TException, IOException {
        HiveConf hiveConf = new HiveConf();
        HiveTestUtils.addResource(hiveConf, args);

        int backlogCount = hiveConf.getInt("hive.server.backlog.count", 0);
        int port = hiveConf.getInt("hive.server.listener.port", 8081);
        ServerSocket serverSocket = new ServerSocket(port, backlogCount);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        while(true) {
           Socket socket = serverSocket.accept();
           InputStream in = socket.getInputStream();
           OutputStream out = socket.getOutputStream();
           int read = in.read();
           out.write(2);
           out.flush();
           in.close();
           out.close();
           socket.close();
        }
    }
}

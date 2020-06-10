package cn.kayleh.diyTomcat;

import cn.hutool.core.util.NetUtil;
import cn.kayleh.http.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: Wizard
 * @Date: 2020/6/7 20:41
 */
public class Bootstrap {
    public static void main(String[] args) {
        try {
            //本服务器使用的端口号是8888
            int port = 8888;
            if (!NetUtil.isUsableLocalPort(port)) {
                //判断端口是否被占用,true表示没有被占用
                System.out.println(port + " 端口已经被占用了，排查并关闭本端口");
                return;
            }
            //在端口8888上启动ServerSocket。服务端和浏览器通信是通过Socket进行通信的，所以这里需要启动一个 ServerSocket。
            ServerSocket serverSocket = new ServerSocket(port);

            //套了一层循环，处理掉一个Socket链接请求之后，再处理下一个链接请求。
            while (true) {

                //表示收到一个浏览器客户端的请求
                Socket accept = serverSocket.accept();

                Request request = new Request(accept);

                System.out.println("浏览器的输入信息： \r\n" + request.getRequestString());
                System.out.println("URI：" + request.getUri());

                //打开输出流，准备给客户端输出信息
                OutputStream outputStream = accept.getOutputStream();

                //准备发送给给客户端的数据。
                String response_head = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n\r\n";
                String responseString = "Hello DIY Tomcat from kayleh.cn";
                responseString = response_head + responseString;

                //把字符串转换成字节数组发送出去
                outputStream.write(responseString.getBytes());
                outputStream.flush();

                //关闭客户端对应的 socket
                accept.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

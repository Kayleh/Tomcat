package cn.kayleh.diyTomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: Wizard
 * @Date: 2020/6/7 20:41
 */
public class Bootstrap {
    public static void main(String[] args) {
        try {
            logJVM();
            //本服务器使用的端口号是8888
            int port = 8888;
//            if (!NetUtil.isUsableLocalPort(port)) {
//                //判断端口是否被占用,true表示没有被占用
//                System.out.println(port + " 端口已经被占用了，排查并关闭本端口");
//                return;
//            }
            //在端口8888上启动ServerSocket。服务端和浏览器通信是通过Socket进行通信的，所以这里需要启动一个 ServerSocket。
            ServerSocket serverSocket = new ServerSocket(port);

            //套了一层循环，处理掉一个Socket链接请求之后，再处理下一个链接请求。
            while (true) {

                //表示收到一个浏览器客户端的请求
                Socket accept = serverSocket.accept();

                Request request = new Request(accept);

                System.out.println("浏览器的输入信息： \r\n" + request.getRequestString());
                System.out.println("URI：" + request.getUri());

                Response response = new Response();

                String uri = request.getUri();

                // 首先判断 uri 是否为空，如果为空就不处理了。 什么情况为空呢？ 在 TestTomcat 里的 NetUtil.isUsableLocalPort(port) 这段代码就会导致为空。
                if (null == uri) continue;
                System.out.println(uri);
                // 如果是 "/", 那么依然返回原字符串。
                if ("/".equals(uri)) {
                    String html = "Hello DIY Tomcat from kayleh.cn";
                    response.getWriter().println(html);
                } else {
                    //如果访问的是a.html ，
                    // URI地址为/a.html ,
                    // fileName为 a.html
                    String fileName = StrUtil.removePrefix(uri, "/");
                    File file = FileUtil.file(Constant.rootFolder, fileName);
                    if (file.exists()) {
                        //如果文件存在
                        String fileContent = FileUtil.readUtf8String(file);
                        response.getWriter().println(fileContent);
                    } else {
                        response.getWriter().println("File Not Found");
                    }
                }

                handle200(accept, response);
//                //打开输出流，准备给客户端输出信息
//                OutputStream outputStream = accept.getOutputStream();
//
//                //准备发送给给客户端的数据。
//                String response_head = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n\r\n";
//                String responseString = "Hello DIY Tomcat from kayleh.cn";
//                responseString = response_head + responseString;
//
//                //把字符串转换成字节数组发送出去
//                outputStream.write(responseString.getBytes());
//                outputStream.flush();
//
//                //关闭客户端对应的 socket
//                accept.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //像 tomcat 那样 一开始打印 jvm 信息
    private static void logJVM() {
        //这里获取日志对象的方式是 LogFactory.get() ，这种方式很方便，否则就要在每个类里面写成
        //static Logger logger = Logger.getLogger(XXX.class)
        //每个类都要写，是很繁琐的，所以我很喜欢 Hutool 这种获取日志的方式。
        //logJVM 会把 jvm 信息都打印出来，看上去就是如图所示的样子了。
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "Kayleh DiyTomcat/1.0.1");
        infos.put("Server built", "2020-06-11 15:55:01");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key+":\t\t"+infos.get(key));
        }
    }

    private static void handle200(Socket socket, Response response) throws IOException {
        String contentType = response.getContentType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType);

        byte[] head = headText.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);
        socket.close();

    }
}

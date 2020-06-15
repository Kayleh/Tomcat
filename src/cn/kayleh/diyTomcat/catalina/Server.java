package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.ThreadPoolUtil;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Wizard
 * @Date: 2020/6/15 14:38
 */
public class Server {
    private Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        logJVM();
        init();
    }

    private void init() {
        try {
            //本服务器使用的端口号是8888
            int port = 8888;
            //在端口8888上启动ServerSocket。服务端和浏览器通信是通过Socket进行通信的，所以这里需要启动一个 ServerSocket。
            ServerSocket serverSocket = new ServerSocket(port);
            //套了一层循环，处理掉一个Socket链接请求之后，再处理下一个链接请求。
            while (true) {
                //表示收到一个浏览器客户端的请求
                Socket accept = serverSocket.accept();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(accept, service);
                            Response response = new Response();
                            String uri = request.getUri();
                            if (null == uri) {
                                return;
                            }
                            System.out.println("uri:" + uri);
                            Context context = request.getContext();
                            if ("/".equals(uri)) {
                                String html = "Hello DIY Tomcat from Kayleh.cn";
                                response.getWriter().println(html);
                            } else {
                                //如果访问的是a.html ，
                                // URI地址为/a.html ,
                                // fileName为 a.html
                                String fileName = StrUtil.removePrefix(uri, "/");
                                File file = FileUtil.file(context.getDocBase(), fileName);
                                if (file.exists()) {
                                    //如果文件存在
                                    String fileContent = FileUtil.readUtf8String(file);
                                    response.getWriter().println(fileContent);
                                    //耗时任务只的是访问某个页面，比较消耗时间，比如连接数据库什么的。
                                    // 这里为了简化，故意设计成访问 timeConsume.html会花掉1秒钟。
                                    if (fileName.equals("TimeConsume.html")) {
                                        ThreadUtil.sleep(1000);
                                    }
                                } else {
                                    handle404(accept, uri);
                                    return;
                                }
                            }
                            handle200(accept, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (!accept.isClosed())
                                    accept.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                ThreadPoolUtil.run(runnable);
            }
        } catch (IOException e) {
            LogFactory.get().error(e);
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
            LogFactory.get().info(key + ":\t\t" + infos.get(key));
        }
    }

    private void handle200(Socket accept, Response response) throws IOException {
        String contentType = response.getContentType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType);

        byte[] head = headText.getBytes();
        byte[] body = response.getBody();

        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);
        OutputStream os = accept.getOutputStream();
        os.write(responseBytes);
//        accept.close();
    }

    protected void handle404(Socket socket, String uri) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String responseText = StrUtil.format(Constant.textFormat_404, uri, uri);
        //响应文本 = 响应头+响应体
        responseText = Constant.response_head_404 + responseText;
        byte[] responseByte = responseText.getBytes("utf-8");
        outputStream.write(responseByte);
    }
}

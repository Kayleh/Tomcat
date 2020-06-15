package cn.kayleh.diyTomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.catalina.Engine;
import cn.kayleh.diyTomcat.catalina.Host;
import cn.kayleh.diyTomcat.catalina.Service;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.ServerXMLUtil;
import cn.kayleh.diyTomcat.util.ThreadPoolUtil;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * @Author: Wizard
 * @Date: 2020/6/7 20:41
 */
public class Bootstrap {
    //声明一个 contextMap 用于存放路径和Context 的映射。
    public static Map<String, Context> contextMap = new HashMap<>();


    public static void main(String[] args) {
        try {
            logJVM();

            Service service = new Service();

            scanContextOnWebAppsFolder();
            scanContextsInServerXml();

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

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(accept, service.getEngine());
                            Response response = new Response();
                            String uri = request.getUri();
                            if (null == uri) return;
                            System.out.println(uri);

                            Context context = request.getContext();

                            if ("/".equals(uri)) {
                                String html = "Hello DIY Tomcat from kayleh.cn";
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
                                    if (fileName.equals("timeConsume.html")) {
                                        ThreadUtil.sleep(1000);
                                    }
                                } else {
                                    response.getWriter().println("File Not Found");
                                }
                            }
                            handle200(accept, response);
                        } catch (IOException e) {
                            e.printStackTrace();
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

    //创建scanContextsInServerXML， 通过 ServerXMLUtil 获取 context, 放进 contextMap里。
    private static void scanContextsInServerXml() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }


    //创建 scanContextsOnWebAppsFolder 方法，用于扫描 webapps 文件夹下的目录，对这些目录调用 loadContext 进行加载。
    private static void scanContextOnWebAppsFolder() {
        //列出webapps下的每一个文件夹
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory())
                continue;
            loadContent(folder);
        }
    }

    //加载这个目录成为 Context 对象。
    //如果是 ROOT，那么path 就是 "/", 如果是 a, 那么path 就是 "/a", 然后根据 path 和 它们所处于的路径创建 Context 对象。
    //然后把这些对象保存进 contextMap，方便后续使用。
    private static void loadContent(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path))
            path = "/";
        else path = "/" + path;

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);

        contextMap.put(context.getPath(), context);

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

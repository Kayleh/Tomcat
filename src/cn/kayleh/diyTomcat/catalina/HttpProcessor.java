package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.WebXMLUtil;
import cn.kayleh.diyTomcat.webappservlet.HelloServlet;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Author: Wizard
 * @Date: 2020/6/18 16:46
 */
//处理请求
public class HttpProcessor {
    public void execute(Socket acept, Request request, Response response) {
        try {
            String uri = request.getUri();
            if (null == uri) {
                return;
            }
            Context context = request.getContext();

            String servletClassName = context.getServletClassName(uri);

            //通过 context 获取 servletClassName， 如果是空就表示不是访问的 servlet。
            //通过类名 servletClassName 实例化 servlet 对象，然后调用其 doGet 方法。
            if (null != servletClassName) {
                Object servletObject = ReflectUtil.newInstance(servletClassName);
                ReflectUtil.invoke(servletObject, "doGet", request, response);
            } else {

                if ("/500.html".equals(uri)) {
                    throw new Exception("this is a deliberately created exception");
                }
                if ("/hello".equals(uri)) {
                    HelloServlet helloServlet = new HelloServlet();
                    helloServlet.doGet(request, response);
                } else {
                    if ("/".equals(uri))
                        uri = WebXMLUtil.getWelcomeFile(request.getContext());

                    //如果访问的是a.html ，
                    // URI地址为/a.html ,
                    // fileName为 a.html
                    String fileName = StrUtil.removePrefix(uri, "/");
                    File file = FileUtil.file(context.getDocBase(), fileName);
                    if (file.exists()) {
                        //如果文件存在
                        //格式
                        String extName = FileUtil.extName(file);
                        String mimeType = WebXMLUtil.getMimeType(extName);
                        response.setContentType(mimeType);

                        byte[] bytes = FileUtil.readBytes(file);
                        response.setBody(bytes);
//                                String fileContent = FileUtil.readUtf8String(file);
//                                response.getWriter().println(fileContent);

                        //耗时任务只的是访问某个页面，比较消耗时间，比如连接数据库什么的。
                        // 这里为了简化，故意设计成访问 timeConsume.html会花掉1秒钟。

                        if (fileName.equals("TimeConsume.html")) {
                            ThreadUtil.sleep(1000);
                        }
                    } else {
                        handle404(acept, uri);
                        return;
                    }
                }
            }
            handle200(acept, response);
        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(acept, e);
        } finally {
            try {
                if (!acept.isClosed())
                    acept.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void handle200(Socket accept, Response response) throws IOException {
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

    private void handle404(Socket socket, String uri) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String responseText = StrUtil.format(Constant.textFormat_404, uri, uri);
        //响应文本 = 响应头+响应体
        responseText = Constant.response_head_404 + responseText;
        byte[] responseByte = responseText.getBytes("utf-8");
        outputStream.write(responseByte);
    }

    private void handle500(Socket accept, Exception e) {
        try {
            OutputStream outputStream = accept.getOutputStream();
            //e.getStackTrace(); 拿到 Exception 的异常堆栈，比如平时我们看到一个报错，都会打印最哪个类的哪个方法，
            // 依次调用过来的信息。 这个信息就放在这个 StackTrace里，是个 StackTraceElement 数组。
            //然后准备个 StringBuffer() , 首先把 e.toString() 信息放进去， 然后挨个把这些堆栈信息放进去，
            // 就会组成如图所示的这样一个 html 效果了。
            StackTraceElement[] stackTrace = e.getStackTrace();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(e.toString());
            stringBuffer.append("\r\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                stringBuffer.append("\t");
                stringBuffer.append(stackTraceElement.toString());
                stringBuffer.append("\r\n");
            }
            String message = e.getMessage();
            //有时候消息太长，超过20个，截短一点方便显示
            if (null != message && message.length() > 20) {
                message = message.substring(0, 19);
            }
            String format = StrUtil.format(Constant.textFormat_500, message, e.toString(), stringBuffer.toString());
            format = Constant.response_head_500 + format;
            byte[] responseBytes = format.getBytes();
            outputStream.write(responseBytes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
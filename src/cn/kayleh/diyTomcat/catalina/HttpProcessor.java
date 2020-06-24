package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.servlets.DefaultServlet;
import cn.kayleh.diyTomcat.servlets.InvokerServlet;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.http.Request;
import cn.kayleh.diyTomcat.http.Response;
import cn.kayleh.diyTomcat.util.SessionManager;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

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

            prepareSession(request, response);

            Context context = request.getContext();

            String servletClassName = context.getServletClassName(uri);

            //通过 context 获取 servletClassName， 如果是空就表示不是访问的 servlet。
            //通过类名 servletClassName 实例化 servlet 对象，然后调用其 doGet 方法。
            if (null != servletClassName) {
                InvokerServlet.getInstance().service(request, response);
//                Object servletObject = ReflectUtil.newInstance(servletClassName);
//                ReflectUtil.invoke(servletObject, "doGet", request, response);
            } else {
                DefaultServlet.getInstance().service(request, response);
            }

            if (Constant.CODE_200 == response.getStatus()) {
                handle200(acept, response);
                return;
            }
            if (Constant.CODE_404 == response.getStatus()) {
                handle404(acept, uri);
                return;
            }
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
        String cookiesHeader = response.getCookiesHeader();

        headText = StrUtil.format(headText, contentType, cookiesHeader);

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
            byte[] responseBytes = format.getBytes("utf-8");
            outputStream.write(responseBytes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prepareSession(Request request, Response response) {
        String jSessionId = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jSessionId, request, response);
        request.setSession(session);
    }
}
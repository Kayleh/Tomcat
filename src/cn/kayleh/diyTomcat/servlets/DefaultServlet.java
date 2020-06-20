package cn.kayleh.diyTomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.WebXMLUtil;
import cn.kayleh.diyTomcat.webappservlet.HelloServlet;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: Wizard
 * @Date: 2020/6/20 13:12
 */
public class DefaultServlet {
    private static DefaultServlet instance = new DefaultServlet();

    public static synchronized DefaultServlet getInstance() {
        return instance;
    }

    public DefaultServlet() {
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        Context context = request.getContext();

        String uri = request.getUri();


        if ("/500.html".equals(uri)) {
            throw new RuntimeException("this is a deliberately created exception");
        }
//        if ("/hello".equals(uri)) {
//            HelloServlet helloServlet = new HelloServlet();
//            helloServlet.doGet(request, response);
//        } else {
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
            response.setStatus(Constant.CODE_200);
        } else {
            response.setStatus(Constant.CODE_404);
//                handle404(acept, uri);
            return;
        }
    }
}


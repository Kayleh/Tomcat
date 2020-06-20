package cn.kayleh.diyTomcat.servlets;

import cn.hutool.core.util.ReflectUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.http.Request;
import cn.kayleh.http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理Servlet
 * @Author: Wizard
 * @Date: 2020/6/20 10:37
 */
public class InvokerServlet extends HttpServlet {
    private static InvokerServlet instance = new InvokerServlet();

    public static synchronized InvokerServlet getInstance() {
        return instance;
    }

    private InvokerServlet() {
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);

        Object servletObject = ReflectUtil.newInstance(servletClassName);
        ReflectUtil.invoke(servletObject, "service", request, response);

    }
}

package cn.kayleh.diyTomcat.servlets;

import cn.hutool.core.util.ReflectUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.http.Request;
import cn.kayleh.diyTomcat.http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理Servlet
 *
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

        try {
            Class servletClass = context.getWebappClassLoader().loadClass(servletClassName);
            System.out.println("servletClass:" + servletClass);
            System.out.println("servletClass'classLoader:" + servletClass.getClassLoader());
            Object servletObject = ReflectUtil.newInstance(servletClass);
            ReflectUtil.invoke(servletObject, "service", request, response);

            //表示处理成功了
            response.setStatus(Constant.CODE_200);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

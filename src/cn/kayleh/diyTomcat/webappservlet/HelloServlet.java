package cn.kayleh.diyTomcat.webappservlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: Wizard
 * @Date: 2020/6/18 17:06
 */
public class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().println("Hello DIY Tomcat from HelloServlet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package cn.kayleh.diyTomcat.http;

import cn.kayleh.diyTomcat.catalina.HttpProcessor;
import org.apache.tools.ant.taskdefs.condition.Http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @Author: Wizard
 * @Date: 2020/6/25 10:59
 */
public class ApplicationRequestDispatcher implements RequestDispatcher {

    private String uri;

    public ApplicationRequestDispatcher(String uri) {
        // 如果是跳转到 hello 修改为 /hello
        if (!uri.startsWith("/"))
            uri = "/" + uri;
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;

        //修正uri
        request.setUri(uri);

        HttpProcessor httpProcessor = new HttpProcessor();
        httpProcessor.execute(request.getSocket(), request, response);
        request.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }
}

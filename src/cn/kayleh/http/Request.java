package cn.kayleh.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 16:10
 */

import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.Bootstrap;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.catalina.Engine;
import cn.kayleh.diyTomcat.catalina.Host;
import cn.kayleh.diyTomcat.catalina.Service;
import cn.kayleh.diyTomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    //创建 Request 对象用来解析 requestString 和 uri。
    private String requestString;
    private String uri;
    private Socket socket;

    private Context context;
    private Service service;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        parseHttpRequest();
        if (StrUtil.isEmpty(requestString))
            return;
        parseUri();

        //在构造方法中调用 parseContext(), 倘若当前 Context 的路径不是 "/", 那么要对 uri进行修正，
        // 比如 uri 是 /a/index.html， 获取出来的 Context路径不是 "/”， 那么要修正 uri 为 /index.html。
        parseContext();
        if (!"/".equals(context.getPath()))
            uri = StrUtil.removePrefix(uri, context.getPath());
    }

    //解析Context 的方法， 通过获取uri 中的信息来得到 path. 然后根据这个 path 来获取 Context 对象。
    // 如果获取不到，比如 /b/a.html, 对应的 path 是 /b, 是没有对应 Context 的，那么就获取 "/” 对应的 ROOT Context。
    private void parseContext() {
        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path)
            path = "/";
        else {
            path = "/" + path;
        }

        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(path);
        if (null == context)
            context = engine.getDefaultHost().getContext("/");

    }

    //parseHttpRequest 用于解析 http请求字符串， 这里面就调用了 MiniBrowser里重构的 readBytes 方法。
    private void parseHttpRequest() throws IOException {
        InputStream is = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(is);
        requestString = new String(bytes, "utf-8");
    }

    //解析URI
    private void parseUri() {
        String temp;

        //是否有参数，带了问号就表示有参数，那么对有参数和没参数分别处理一下，就拿到了 uri.
        temp = StrUtil.subBetween(requestString, " ", " ");
        if (!StrUtil.contains(temp, '?')) {
            uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp, '?', false);
        uri = temp;
    }


    public Context getContext() {
        return context;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

}
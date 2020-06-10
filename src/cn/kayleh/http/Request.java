package cn.kayleh.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 16:10
 */

import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    //创建 Request 对象用来解析 requestString 和 uri。
    private String requestString;
    private String uri;
    private Socket socket;
    public Request(Socket socket) throws IOException {
        this.socket = socket;
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString))
            return;
        parseUri();
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

    public String getUri() {
        return uri;
    }

    public String getRequestString(){
        return requestString;
    }

}
package cn.kayleh.diyTomcat.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 16:10
 */

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.catalina.Engine;
import cn.kayleh.diyTomcat.catalina.Service;
import cn.kayleh.diyTomcat.util.MiniBrowser;
import sun.nio.ch.IOUtil;


import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

public class Request extends BaseRequest {
    //创建 Request 对象用来解析 requestString 和 uri。
    private String requestString;
    private String uri;
    private Socket socket;

    private Context context;
    private Service service;

    private String method;

    //查询字符串和参数Map
    private String queryString;
    private Map<String, String[]> parameterMap;
    //声明 headerMap用于存放头信息
    private Map<String, String> headerMap;

    private Cookie[] cookies;

    private HttpSession session;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;

        this.parameterMap = new HashMap<>();

        this.headerMap = new HashMap<>();

        parseHttpRequest();
        if (StrUtil.isEmpty(requestString))
            return;
        parseUri();

        //在构造方法中调用 parseContext(), 倘若当前 Context 的路径不是 "/", 那么要对 uri进行修正，
        // 比如 uri 是 /a/index.html， 获取出来的 Context路径不是 "/”， 那么要修正 uri 为 /index.html。
        parseContext();

        parseMethod();

        if (!"/".equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }
        parseParameters();
        parseHeaders();
        parseCookies();
    }

    //从 cookie 中获取sessionid
    public String getJSessionIdFromCookie() {
        if (null == cookies)
            return null;
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    //从 http 请求协议中解析出 Cookie
    private void parseCookies() {
        List<Cookie> cookieList = new ArrayList<>();
        String cookies = headerMap.get("cookie");
        if (null != cookies) {
            String[] pairs = StrUtil.split(cookies, ";");
            for (String pair : pairs) {
                if (StrUtil.isBlank(pair))
                    continue;
                // System.out.println(pair.length());
                // System.out.println("pair:"+pair);
                String[] segs = StrUtil.split(pair, "=");
                String name = segs[0].trim();
                String value = segs[1].trim();
                Cookie cookie = new Cookie(name, value);
                cookieList.add(cookie);
            }
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    //根据 get 和 post 方式分别解析参数。 需要注意的是，参数Map里存放的值是 字符串数组类型
    private void parseParameters() {
        System.out.println(requestString);
        //GET 的参数是放在 uri 里的
        //POST 的参数是放在请求最后的请求体里的
        if ("GET".equals(this.getMethod())) {
            String url = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(url, '?')) {
                queryString = StrUtil.subAfter(url, '?', false);
            }
        }
        if ("POST".equals(this.getMethod())) {
            queryString = StrUtil.subAfter(requestString, "\r\n\r\n", false);
        }
        if (null == queryString || 0 == queryString.length()) {
            return;
        }
        queryString = URLUtil.decode(queryString);
        String[] parameterValues = queryString.split("&");
        if (null != parameterValues) {
            for (String parameterValue : parameterValues) {
                String[] nameValues = parameterValue.split("=");
                String name = nameValues[0];
                String value = nameValues[1];
                String[] values = parameterMap.get(name);
                if (null == values) {
                    values = new String[]{value};
                    parameterMap.put(name, values);
                } else {
                    values = ArrayUtil.append(values, value);
                    parameterMap.put(name, values);
                }
            }
        }
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public String getMethod() {
        return method;
    }


    public void parseHeaders() {
        StringReader stringReader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(stringReader, lines);
        //！！跳过了第一行的请求体
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (0 == line.length()) {
                break;
            }
            String[] segs = line.split(":");
            String headName = segs[0].toLowerCase();
            String headValue = segs[1];
            headerMap.put(headName, headValue);
        }
    }

    //提供解析方法，其实就是取第一个空格之前的数据。
    private void parseMethod() {
        method = StrUtil.subBefore(requestString, " ", false);
    }

    //解析Context 的方法， 通过获取uri 中的信息来得到 path. 然后根据这个 path 来获取 Context 对象。
    // 如果获取不到，比如 /b/a.html, 对应的 path 是 /b, 是没有对应 Context 的，那么就获取 "/” 对应的 ROOT Context。
    private void parseContext() {
        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(uri);
        if (null != context) {
            return;
        }
        String path = StrUtil.subBetween(uri, "/", "/");
        if (null == path)
            path = "/";
        else {
            path = "/" + path;
        }

        context = engine.getDefaultHost().getContext(path);
        if (null == context)
            context = engine.getDefaultHost().getContext("/");

    }

    //parseHttpRequest 用于解析 http请求字符串， 这里面就调用了 MiniBrowser里重构的 readBytes 方法。
    private void parseHttpRequest() throws IOException {
        InputStream is = this.socket.getInputStream();

        //false  如果读取到的数据不够 bufferSize ,那么就不继续读取了。
        //不能用过 true 呢？ 因为浏览器默认使用长连接，发出的连接不会主动关闭，那么 Request 读取数据的时候 就会卡在那里了
        byte[] bytes = MiniBrowser.readBytes(is, false);
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

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        if (null != values && 0 != values.length)
            return values[0];
        return null;
    }

    @Override
    public String getHeader(String name) {
        if (null == name) {
            return null;
        }
        name = name.toLowerCase();
        return headerMap.get(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set keys = headerMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public int getIntHeader(String name) {
        String value = headerMap.get(name);
        return Convert.toInt(value, 0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String getRealPath(String path) {
        return context.getServletContext().getRealPath(path);
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
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


    @Override
    public String getContextPath() {
        String path = this.getContext().getPath();
        if ("/".equals(path)) {
            return "";
        }
        return path;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80;// Work around java.net.URL bug
        }
        url.append(scheme);// http
        url.append("://");//  http://
        url.append(getServerName());
        if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() {
        return uri;
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getServerPort() {
        return super.getServerPort();
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        String temp = isa.getAddress().toString();
        return StrUtil.subAfter(temp, "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
        return isa.getHostName();
    }

    @Override
    public int getRemotePort() {
        return socket.getPort();
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    //获取协议
    @Override
    public String getProtocol() {
        return "HTTP:/1.1";
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
}
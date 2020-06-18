package cn.kayleh.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 16:10
 */

import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.catalina.Engine;
import cn.kayleh.diyTomcat.catalina.Service;
import cn.kayleh.diyTomcat.util.MiniBrowser;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class Request implements HttpServletRequest {
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

        if (!"/".equals(context.getPath())) {

            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)) {
                uri = "/";
            }
        }

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
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String s) {
        return 0;
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
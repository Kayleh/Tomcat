package cn.kayleh.diyTomcat.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 20:28
 */

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.http.Cookie;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Response extends BaseResponse {
    //用于存放返回的 html 文本
    private StringWriter stringWriter;
    private PrintWriter writer;
    private String contentType;
    private byte[] body;
    private int status;

    private List<Cookie> cookies;

    public Response() {
        this.stringWriter = new StringWriter();
        //这个PrintWriter 其实是建立在 stringWriter的基础上的，所以 response.getWriter().println();
        // 写进去的数据最后都写到 stringWriter 里面去了。
        this.writer = new PrintWriter(stringWriter);
        //contentType就是对应响应头信息里的 Content-type ，默认是 "text/html"。
        this.contentType = "text/html";

        this.cookies = new ArrayList<>();
    }

    //  把Cookie集合转换成 cookie Header。
    public String getCookiesHeader() {
        if (null == cookies)
            return "";

        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);

        StringBuffer sb = new StringBuffer();
        for (Cookie cookie : getCookies()) {
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
            if (-1 != cookie.getMaxAge()) { //-1 mean forever
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.MINUTE, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append("; ");
            }
            if (null != cookie.getPath()) {
                sb.append("Path=" + cookie.getPath());
            }
        }

        return sb.toString();
    }

    public List<Cookie> getCookies() {
        return this.cookies;
    }

    //检擦是否为重写！！！
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }


    //response.getWriter().println();
    public PrintWriter getWriter() {
        return writer;
    }


    public String getContentType() {
        return contentType;
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() throws UnsupportedEncodingException {

        if (null == body) {
            String content = stringWriter.toString();
            body = content.getBytes("utf-8");
        }
        //当body 不为空的时候，直接返回 body
        return body;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }
}

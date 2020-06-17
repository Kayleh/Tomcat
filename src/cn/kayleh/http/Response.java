package cn.kayleh.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 20:28
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;


public class Response {
    //用于存放返回的 html 文本
    private StringWriter stringWriter;
    private PrintWriter writer;
    private String ContentType;

    public Response() {
        this.stringWriter = new StringWriter();
        //这个PrintWriter 其实是建立在 stringWriter的基础上的，所以 response.getWriter().println();
        // 写进去的数据最后都写到 stringWriter 里面去了。
        this.writer = new PrintWriter(stringWriter);
        //contentType就是对应响应头信息里的 Content-type ，默认是 "text/html"。
        this.ContentType = "text/html";
    }

    public String getContentType() {
        return ContentType;
    }

    public void setContentType(String contentType) {
        ContentType = contentType;
    }

    //response.getWriter().println();
    public PrintWriter getWriter() {
        return writer;
    }

    public byte[] getBody() {
        String content = stringWriter.toString();
        byte[] body = content.getBytes();
        return body;
    }
}

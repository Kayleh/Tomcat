package cn.kayleh.http;

/**
 * @Author: Wizard
 * @Date: 2020/6/10 20:28
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;


public class Response extends BaseResponse {
    //用于存放返回的 html 文本
    private StringWriter stringWriter;
    private PrintWriter writer;
    private String ContentType;
    private byte[] body;

    public Response() {
        this.stringWriter = new StringWriter();
        //这个PrintWriter 其实是建立在 stringWriter的基础上的，所以 response.getWriter().println();
        // 写进去的数据最后都写到 stringWriter 里面去了。
        this.writer = new PrintWriter(stringWriter);
        //contentType就是对应响应头信息里的 Content-type ，默认是 "text/html"。
        this.ContentType = "text/html";
    }

    //response.getWriter().println();
    public PrintWriter getWriter() {
        return writer;
    }


    public String getContentType() {
        return ContentType;
    }



    public void setContentType(String contentType) {
        this.ContentType = contentType;
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


}

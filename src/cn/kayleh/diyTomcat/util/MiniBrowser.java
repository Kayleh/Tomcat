package cn.kayleh.diyTomcat.util;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Wizard
 * @Date: 2020/6/9 10:04
 */
public class MiniBrowser {

    public static void main(String[] args) throws Exception {
        String url = "http://static.how2j.cn/diytomcat.html";
        String contentString = getContentString(url, false);
        System.out.println(contentString);
        String httpString = getHttpString(url, false);
        System.out.println(httpString);
    }

    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false);
    }

    public static String getContentString(String url) {
        return getContentString(url, false);
    }

    public static String getContentString(String url, boolean gzip) {
        byte[] result = getContentBytes(url, gzip);
        if (null == result)
            return null;
        try {
            return new String(result, "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getContentBytes(String url, boolean gzip) {
        byte[] response = getHttpBytes(url, gzip);
        byte[] doubleReturn = "\r\n\r\n".getBytes();

        int pos = -1;
        for (int i = 0; i < response.length - doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);

            if (Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        if (-1 == pos)
            return null;

        pos += doubleReturn.length;

        byte[] result = Arrays.copyOfRange(response, pos, response.length);
        return result;
    }

    public static String getHttpString(String url, boolean gzip) {
        byte[] bytes = getHttpBytes(url, gzip);
        return new String(bytes).trim();
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false);
    }

    public static byte[] getHttpBytes(String url, boolean gzip) {
        byte[] result = null;
        try {
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if (-1 == port)
                port = 80;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);
            Map<String, String> requestHeaders = new HashMap<>();

            requestHeaders.put("Host", u.getHost() + ":" + port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "kayleh mini brower / java1.8");

            if (gzip)
                requestHeaders.put("Accept-Encoding", "gzip");

            String path = u.getPath();
            if (path.length() == 0)
                path = "/";

            String firstLine = "GET " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headerLine);
            }

            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(httpRequestString);
            InputStream is = client.getInputStream();


//            //准备一个 1024长度的缓存，不断地从输入流读取数据到这个缓存里面去。
//            int buffer_size = 1024;
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            byte buffer[] = new byte[buffer_size];
//            while(true) {
//                //如果读取到的长度是 -1，那么就表示到头了，就停止循环
//                int length = is.read(buffer);
//                if(-1==length)
//                    break;
//                //如果读取到的长度小于 buffer_size, 说明也读完了
//                byteArrayOutputStream.write(buffer, 0, length);
//                if(length!=buffer_size) {
//                    break;
//                }
//            }
//            //把读取到的数据，根据实际长度，写出到 一个字节数组输出流里。
//            result = byteArrayOutputStream.toByteArray();

            result = readBytes(is);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                result = e.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
            }
        }

        return result;

    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        //准备一个 1024长度的缓存，不断地从输入流读取数据到这个缓存里面去。
        int buffer_size = 1024;
        byte buffer[] = new byte[buffer_size];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            //如果读取到的长度是 -1，那么就表示到头了，就停止循环
            int length = inputStream.read(buffer);
            if (-1 == length)
                break;
            //如果读取到的长度小于 buffer_size, 说明也读完了
            byteArrayOutputStream.write(buffer, 0, length);
            if (length != buffer_size)
                break;
        }
        //把读取到的数据，根据实际长度，写出到 一个字节数组输出流里。
        byte[] result = byteArrayOutputStream.toByteArray();
        return result;
    }
}
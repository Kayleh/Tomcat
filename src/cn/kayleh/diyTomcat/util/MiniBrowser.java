package cn.kayleh.diyTomcat.util;

import cn.hutool.http.HttpUtil;

import java.io.*;
import java.net.InetSocketAddress;
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
        String url = "http://static.Kayleh.cn/diytomcat.html";
        String contentString = getContentString(url, false);
        System.out.println(contentString);
        String httpString = getHttpString(url, false);
        System.out.println(httpString);
    }

    public static byte[] getContentBytes(String url, Map<String, Object> params, boolean isGet) {
        return getContentBytes(url, false, params, isGet);
    }

    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false, null, true);
    }

    public static byte[] getContentBytes(String url, boolean gzip) {
        return getContentBytes(url, gzip, null, true);
    }

    public static byte[] getContentBytes(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        byte[] response = getHttpBytes(url, gzip, params, isGet);
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


    public static String getContentString(String url) {
        return getContentString(url, false, null, true);
    }

    public static String getContentString(String url, boolean gzip) {
        return getContentString(url, gzip, null, true);
    }

    public static String getContentString(String url, Map<String, Object> params, boolean isGet) {
        return getContentString(url, false, params, isGet);
    }

    public static String getContentString(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        byte[] result = getContentBytes(url, gzip, params, isGet);
        if (null == result) {
            return null;
        }
        try {
            return new String(result, "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false, null, true);
    }

    public static String getHttpString(String url, boolean gzip) {

        return getHttpString(url, gzip, null, true);
    }

    public static String getHttpString(String url, Map<String, Object> params, boolean isGet) {
        return getHttpString(url, false, params, isGet);

    }

    public static String getHttpString(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        byte[] bytes = getHttpBytes(url, gzip, params, isGet);
        return new String(bytes).trim();
    }


    public static byte[] getHttpBytes(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        String method = isGet ? "GET" : "POST";
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

            //GET
            if (null != params && isGet) {
                //GET 的参数是放在 uri 里的
                String paramsString = HttpUtil.toParams(params);
                path = path + "?" + paramsString;
            }
            //空格
            String firstLine = method + " " + path + " HTTP/1.1\r\n";
//            String firstLine = "GET " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headerLine);
            }

            if (null != params && !isGet) {
                //POST 的参数是放在请求最后的请求体里的
                String paramString = HttpUtil.toParams(params);
                httpRequestString.append("\r\n");
                httpRequestString.append(paramString);
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

            result = readBytes(is, true);
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

    public static byte[] readBytes(InputStream inputStream, boolean fully) throws IOException {
        //准备一个 1024长度的缓存，不断地从输入流读取数据到这个缓存里面去。
        int buffer_size = 1024;
        byte[] buffer = new byte[buffer_size];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            //如果读取到的长度是 -1，那么就表示到头了，就停止循环
            int length = inputStream.read(buffer);
            if (-1 == length)
                break;
            //如果读取到的长度小于 buffer_size, 说明也读完了
            byteArrayOutputStream.write(buffer, 0, length);
            // fully 表示是否完全读取。
            if (!fully && length != buffer_size)
                break;

        }
        //把读取到的数据，根据实际长度，写出到 一个字节数组输出流里。
        byte[] result = byteArrayOutputStream.toByteArray();
        return result;
    }
}
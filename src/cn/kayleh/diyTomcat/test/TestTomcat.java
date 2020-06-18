package cn.kayleh.diyTomcat.test;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.kayleh.diyTomcat.util.MiniBrowser;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.jsoup.internal.StringUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.kayleh.diyTomcat.util.MiniBrowser.getContentBytes;

/**
 * @Author: Wizard
 * @Date: 2020/6/9 13:35
 */
public class TestTomcat {
    //预先定义端口和ip地址，方便修改。
    private static int port = 8888;
    private static String ip = "127.0.0.1";

    //在测试启动之前会先检查diytomcat是否已经启动了，如果未启动，就不用做测试了，并且给出提示信息。
    @BeforeClass
    public static void beforeClass() {
        //所有测试开始前看diy tomcat 是否已经启动了
        if (NetUtil.isUsableLocalPort(port)) {
            System.err.println("请先启动 位于端口: " + port + " 的diy tomcat，否则无法进行单元测试");
            System.exit(1);
        } else {
            System.out.println("检测到 diy tomcat已经启动，下面进行单元测试");
        }
    }


    //测试方法，用于访问： http://127.0.0.1:18080/, 并验证返回值是否是 “Hello DIY Tomcat from how2j.cn”，如果不是就会测试失败。
    @Test
    public void testHelloTomcat() {
        String html = getContentString("/");
        Assert.assertEquals(html, "Hello DIY Tomcat from Kayleh.cn");
    }


    @Test
    public void testaHtml() {
        String html = getContentString("/a.html");
        Assert.assertEquals(html, "Hello DIY Tomcat from a.html");
    }

    /**
     * 因为 Bootstrap 是单线程的，来一个请求，处理一个。 处理完毕之后，才能处理下一个。
     * 所以我们在单元测试里准备一个线程池，同时模仿3个同时访问 timeConsume.html，
     * 正是因为 Bootstrap 是单线程的，所以得一个一个地处理，导致3个同时访问，最后累计时间是 3秒以上。
     *
     * @throws InterruptedException
     */
    @Test
    public void testTimeConsumeHtml() throws InterruptedException {
        //准备一个线程池，里面有20根线程。
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));
        //开始计时
        TimeInterval timeInterval = DateUtil.timer();

        //连续执行3个任务，可以简单地理解成3个任务同时开始
        for (int i = 0; i < 3; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    getContentString("/timeConsume.html");
                }
            });
        }
        //shutdown 尝试关闭线程池，但是如果 线程池里有任务在运行，就不会强制关闭，直到任务都结束了，才关闭.
        //awaitTermination 会给线程池1个小时的时间去执行，如果超过1个小时了也会返回，如果在一个小时内任务结束了，就会马上返回。
        //通过这两行代码就可以做到，当3个任务结束时候，才运行后续代码。
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        //获取经过了多长时间的毫秒数，并且断言它是超过3秒的。
        long duration = timeInterval.intervalMs();
        Assert.assertTrue(duration < 3000);

    }

//    @Test
//    public void testaIndex() {
//        String html = getContentString("/a.html");
//        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@a");
//    }
//
//    @Test
//    public void testbIndex() {
//        String html = getContentString("/b/index.html");
//        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@b");
//    }

    @Test
    public void testaIndex() {
        String html = getContentString("/a");
        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@a");
    }

    @Test
    public void testbIndex() {
        String html = getContentString("/b/");
        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@b");
    }

    @Test
    public void test404() {
        //访问某个不存在的 html , 然后断言 返回的 http 响应里包含 HTTP/1.1 404 Not Found,
        // 毕竟返回的整个 http 响应那么长，不好用 equals 来比较，只要包含关键的头信息，就算测试通过啦
        String response = getHttpString("/not_exist.html");
        containAssert(response, "HTTP/1.1 404 Not Found");
    }

    @Test
    public void test500() {
        String response = getHttpString("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
    }

    @Test
    public void testaTxt() {
        String response = getHttpString("/a.txt");
        containAssert(response, "Content-Type: text/plain");
    }

    @Test
    public void testPNG() {
        byte[] bytes = getContentBytes("/logo.png");
        int pngFileLength = 1672;
        Assert.assertEquals(pngFileLength, bytes.length);
    }

    @Test
    public void testPDF() {
        String uri = "/etf.pdf";
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpUtil.download(url, baos, true);
        int pdfFileLength = 3590775;
        Assert.assertEquals(pdfFileLength, baos.toByteArray().length);
    }

    @Test
    public void testHello() {
        String html = getContentString("/hello");
        Assert.assertEquals(html, "Hello DIY Tomcat from HelloServlet");
    }





    //增加一个 containAssert 断言，来判断html 里是否包含某段字符串的断言
    private void containAssert(String html, String string) {
        boolean match = StrUtil.containsAny(html, string);
        Assert.assertTrue(match);
    }

    //增加一个 getHttpString 方法来获取 Http 响应
    public String getHttpString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String http = MiniBrowser.getHttpString(url);
        return http;
    }

    //准备一个工具方法，用来获取网页返回。
    public String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

    private byte[] getContentBytes(String uri) {
        return getContentBytes(uri, false);
    }

    private byte[] getContentBytes(String uri, boolean gzip) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        return MiniBrowser.getContentBytes(url, false);
    }


}

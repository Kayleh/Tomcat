package cn.kayleh.diyTomcat.test;

import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.util.MiniBrowser;
import org.jsoup.internal.StringUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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

    //准备一个工具方法，用来获取网页返回。
    public String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

    //测试方法，用于访问： http://127.0.0.1:18080/, 并验证返回值是否是 “Hello DIY Tomcat from how2j.cn”，如果不是就会测试失败。
    @Test
    public void testHelloTomcat() {
        String html = getContentString("/");
        Assert.assertEquals(html, "Hello DIY Tomcat from kayleh.cn");
    }

}

package cn.kayleh.diyTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/6/13 20:44
 */
public class ServerXMLUtil {
    public static List<Context> getContexts() {
        List<Context> result = new ArrayList<>();
        //获取 server.xml 的内容
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        //转换成 jsoup document
        Document document = Jsoup.parse(xml);

        //查询所有的 Context 节点
        Elements elements = document.select("Context");
        //遍历这些节点，并获取对应的 path和docBase ，以生成 Context 对象， 然后放进 result 返回。
        for (Element element : elements) {
            String path = element.attr("path");
            String docBase = element.attr("docBase");
            Context context = new Context(path, docBase);
            result.add(context);
        }
        return result;
    }

    //解析 host 元素下的 name 属性。
    public static String getHostName() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);

        Elements host = document.select("Host");
        return host.attr("name");
    }
}

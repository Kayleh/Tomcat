package cn.kayleh.diyTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.catalina.Engine;
import cn.kayleh.diyTomcat.catalina.Host;
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

    public static String getServiceName() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);

        Element service = document.select("Service").first();
        return service.attr("name");
    }

    //获取Engine下的defaultHost的值
    public static String getEngineDefaultHost() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);

        Element host = document.select("Engine").first();
        return host.attr("defaultHost");
    }

    public static List<Host> getHosts(Engine engine) {
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);

        Elements hosts = document.select("Host");
        for (Element e : hosts) {
            String name = e.attr("name");
            Host host = new Host(name, engine);
            result.add(host);
        }
        return result;

    }

}

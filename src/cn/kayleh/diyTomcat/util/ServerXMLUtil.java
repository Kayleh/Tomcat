package cn.kayleh.diyTomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.kayleh.diyTomcat.catalina.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/6/13 20:44
 */
public class ServerXMLUtil {

    //getContexts 方法，传如 Host 参数， 创建 Context 对象的时候带上 host 和 reloadable 参数。
    public static List<Context> getContexts(Host host) {
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
            boolean reloadable = Convert.toBool(element.attr("reloadable"),true);
            Context context = new Context(path, docBase,host,reloadable);
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

    //获取 Connectors 集合
    public static List<Connector> getConnector(Service service) {
        List<Connector> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("Connector");
        for (Element element : elements) {
            int port = Convert.toInt(element.attr("port"));
            Connector connector = new Connector(service);
            connector.setPort(port);
            result.add(connector);
        }
        return result;
    }

}

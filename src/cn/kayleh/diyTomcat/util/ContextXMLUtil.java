package cn.kayleh.diyTomcat.util;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;

/**
 * @Author: Wizard
 * @Date: 2020/6/19 19:43
 */
public class ContextXMLUtil {
    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(Constant.contextXmlFile);
            Document document = Jsoup.parse(xml);
            Element element = document.select("WatchedResource").first();
            return element.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }
}

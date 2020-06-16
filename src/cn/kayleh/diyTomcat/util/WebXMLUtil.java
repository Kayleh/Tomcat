package cn.kayleh.diyTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

import static cn.kayleh.diyTomcat.util.Constant.webXmlFile;

/**
 * @Author: Wizard
 * @Date: 2020/6/16 14:23
 */
public class WebXMLUtil {
    public static String getWelcomeFile(Context context){
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("welcome-file");
        //根据 Context的 docBase 去匹配 web.xml 中的3个文件
        for (Element element : elements) {
            String welcomeFileName = element.text();
            File file = new File(context.getDocBase(), welcomeFileName);
            if (file.exists()){
                return file.getName();
            }
        }
        //如果没有找到，默认返回index.html
        return "index.html";
    }
}

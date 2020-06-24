package cn.kayleh.diyTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static cn.kayleh.diyTomcat.util.Constant.webXmlFile;

/**
 * @Author: Wizard
 * @Date: 2020/6/16 14:23
 */
public class WebXMLUtil {
    private static Map<String, String> mimeTypeMapping = new HashMap<>();

    /**
     * @param extName 文件扩展名
     * @return
     */
    public static synchronized String getMimeType(String extName) {
        if (mimeTypeMapping.isEmpty()) {
            initMimeType();
        }
        String mimeType = mimeTypeMapping.get(extName);
        if (null == mimeType) {
            return "text/html";
        }
        return mimeType;
    }

    private static void initMimeType() {
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document document = Jsoup.parse(xml);

        Elements elements = document.select("mime-mapping");
        for (Element element : elements) {
            String extName = element.select("extension").first().text();
            String mimeType = element.select("mime-type").first().text();
            mimeTypeMapping.put(extName, mimeType);
        }
    }

    //获得欢迎页面
    public static String getWelcomeFile(Context context) {
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("welcome-file");
        //根据 Context的 docBase 去匹配 web.xml 中的3个文件
        for (Element element : elements) {
            String welcomeFileName = element.text();
            File file = new File(context.getDocBase(), welcomeFileName);
            if (file.exists()) {
                return file.getName();
            }
        }
        //如果没有找到，默认返回index.html
        return "index.html";
    }
}

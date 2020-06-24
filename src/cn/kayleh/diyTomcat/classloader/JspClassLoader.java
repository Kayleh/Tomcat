package cn.kayleh.diyTomcat.classloader;

import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.util.Constant;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Wizard
 * @Date: 2020/6/24 20:51
 */
public class JspClassLoader extends URLClassLoader {
    private static Map<String, JspClassLoader> map = new HashMap<>();

    public static void invalidJspClassLoader(String uri, Context context) {
        // key是项目路径 + / + 访问地址
        String key = context.getPath() + "/" + uri;
        map.remove(key);
    }

    public static JspClassLoader getJspClassLoader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        JspClassLoader loader = map.get(key);
        if (null == loader) {
            loader = new JspClassLoader(context);
            map.put(key, loader);
        }
        return loader;
    }

    private JspClassLoader(Context context) {
        super(new URL[]{}, context.getWebClassLoader());
        try {
            String subFolder;
            String path = context.getPath();
            if ("/".equals(path))
                subFolder = "_";
            else
                subFolder = StrUtil.subAfter(path, "/", false);

            File classessFolder = new File(Constant.workFolder, subFolder);
            URL url = new URL("file:" + classessFolder.getAbsolutePath() + "/");
            this.addURL(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

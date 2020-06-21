package cn.kayleh.diyTomcat.classloader;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/6/21 11:04
 */
//WebappClassLoader 是专门用于加载某个 web 应用下的 class 和 jar 的。
//每个 Web 应用都有属于自己专属的 WebClassLoader ， 这样才可以做到同一个名称的类， 在不同的 web 应用里，互不干扰。
public class WebappClassLoader extends URLClassLoader {
    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);

        try {
            File webInfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webInfFolder, "classes");
            File libFolder = new File(classesFolder, "lib");
            URL url;
            url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);
            List<File> jarFiles = FileUtil.loopFiles(libFolder);
            for (File file : jarFiles) {
                url = new URL("file:" + file.getAbsolutePath());
                this.addURL(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

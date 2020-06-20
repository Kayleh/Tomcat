package cn.kayleh.diyTomcat.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * CommonClassLoader 扫描 lib 目录下的jar, 然后通过 addURL 加到当前的库里面去。
 * 这样当调用它 的 loadClass 方法的时候，就会从这些 jar 里面去找了。
 *
 * @Author: Wizard
 * @Date: 2020/6/20 17:55
 */
public class CommonClassLoader extends URLClassLoader {

    public CommonClassLoader() {
        super(new URL[]{});

        try{
            //列出项目工作路径下的lib下的每一个文件
            File workgingFolder = new File(System.getProperty("user.dir"));
            File libFolder = new File(workgingFolder, "lib");
            File[] jarFiles = libFolder.listFiles();
            for (File file : jarFiles) {
                //如果文件是jar包
                if (file.getName().endsWith("jar")){
                    URL url = new URL("file:" + file.getAbsolutePath());
                    this.addURL(url);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}

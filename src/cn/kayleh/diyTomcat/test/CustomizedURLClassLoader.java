package cn.kayleh.diyTomcat.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @Author: Wizard
 * @Date: 2020/6/20 16:12
 */
public class CustomizedURLClassLoader extends URLClassLoader {
    public CustomizedURLClassLoader(URL[] urls) {
        super(urls);
    }

    public static void main(String[] args) throws Exception {
        URL url = new URL("file:d:/project/diyTomcat/jar_4_test/test.jar");
        URL[] urls = new URL[]{url};
        CustomizedURLClassLoader loader = new CustomizedURLClassLoader(urls);

        Class<?> kaylehClass = loader.loadClass("cn.kayleh.diyTomcat.test.HOW2J");
        Object o = kaylehClass.newInstance();
        Method m = kaylehClass.getMethod("hello");
        m.invoke(o);

        System.out.println(kaylehClass.getClassLoader());


//        URL url = new URL("file:d:/project/diytomcat/jar_4_test/test.jar");
//        URL[] urls = new URL[] {url};
//
//        CustomizedURLClassLoader loader1 = new CustomizedURLClassLoader(urls);
//        Class<?> how2jClass1 = loader1.loadClass("cn.how2j.diytomcat.test.HOW2J");
//
//        CustomizedURLClassLoader loader2 = new CustomizedURLClassLoader(urls);
//        Class<?> how2jClass2 = loader2.loadClass("cn.how2j.diytomcat.test.HOW2J");
//
//        System.out.println(how2jClass1==how2jClass2);
    }
}

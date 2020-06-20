package cn.kayleh.diyTomcat.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @Author: Wizard
 * @Date: 2020/6/20 15:20
 */
public class CustomizedClassLoader extends ClassLoader {
    // 定义属性 classesFolder 其值是当前项目下的 classes_4_test 目录
    private File classesFolder = new File(System.getProperty("user.dir"), "classes_4_test");

    protected Class<?> findClass(String QualifiedName) throws ClassNotFoundException {
        byte[] data = loadClassData(QualifiedName);
        return defineClass(QualifiedName, data, 0, data.length);
    }

    // 重写 loadClassData 方法，它会把传进去的全限定类名，匹配到文件：
    private byte[] loadClassData(String fullQualifiedName) throws ClassNotFoundException {
        String fileName = StrUtil.replace(fullQualifiedName, ".", "/") + ".class";
        File classFile = new File(classesFolder, fileName);
        if (!classFile.exists()) {
            throw new ClassNotFoundException();
        }
        return FileUtil.readBytes(fileName);
    }

    public static void main(String[] args) throws Exception {
        CustomizedClassLoader loader = new CustomizedClassLoader();
        Class<?> kaylehClass = loader.loadClass("cn.kayleh.diyTomcat.test.kayleh");
        //并返回改文件的字节数组。
        //重写 findClass 方法，把这个字节数组通过调用 defineClass 方法，就转换成 HOW2J 这个类对应的 Class 对象了。
        //拿到这个类对象之后，通过反射机制，调用其 hello 方法，
        // 就能看到如图所示的字符串：hello, this is kayleh saying "Hello!"
        Object o = kaylehClass.newInstance();
        Method method = kaylehClass.getMethod("hello");
        method.invoke(o);
        System.out.println(kaylehClass.getClassLoader());

    }
}

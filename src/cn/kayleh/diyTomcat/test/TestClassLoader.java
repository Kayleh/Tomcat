package cn.kayleh.diyTomcat.test;

/**
 * @Author: Wizard
 * @Date: 2020/6/20 15:04
 */

public class TestClassLoader {

    public static void main(String[] args) {
        Object o = new Object();
        System.out.println(o);
        Class<?> clazz = o.getClass();
        System.out.println(clazz);

        System.out.println("--");
        System.out.println(Object.class.getClassLoader());
        System.out.println(TestClassLoader.class.getClassLoader());
    }
}

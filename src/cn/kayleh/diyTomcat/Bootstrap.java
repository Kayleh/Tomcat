package cn.kayleh.diyTomcat;

import cn.kayleh.diyTomcat.catalina.Server;
import cn.kayleh.diyTomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;

/**
 * @Author: Wizard
 * @Date: 2020/6/7 20:41
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {


//        Server server = new Server();
//        server.start();
        //  类加载器
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        //  线程上下文类加载器，它表示后续加载的类，都会使用这个 CommonClassLoader
        //  如果没有通过setContextClassLoader方法进行设置的话，
        //  线程将继承其父线程的上下文加载器，java应用运行时的初始线程的上下文类加载器是系统类加载器（这里是由Launcher类设置的）。
        //  在线程中运行的代码可以通过该类加载器来加载类和资源
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        //  设置server的全限定类名
        String serverClassName = "cn.kayleh.diyTomcat.catalina.Server";
        //  加载 Server的class 类对象
        Class<?> serverClazz = commonClassLoader.loadClass(serverClassName);
        //  根据类对象获得实例对象
        Object serverObject = serverClazz.newInstance();
        //  获得类对象的start方法
        Method method = serverClazz.getMethod("start");
        //  调用方法
        method.invoke(serverObject);

        System.out.println(serverClazz.getClassLoader());

        // 不能关闭，否则后续就不能使用
        // commonClassLoader.close()


        System.out.println("   _   _                _             _");
        System.out.println("  | | / /              | |           | |");
        System.out.println("  | |/ /  ____  _    _ | |  ___  ____| |");
        System.out.println("  | | <  /  _  \\\\ \\ / /| | / _ \\(  __` |");
        System.out.println("  | |\\ \\ | (_| | \\ V / | ||  __/| |  | |");
        System.out.println("  |_| \\_\\\\___,_|  \\ /  |_| \\___||_|  |_|");
        System.out.println("                 /_/");

    }
}

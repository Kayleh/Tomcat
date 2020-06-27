package cn.kayleh.diyTomcat.test;

/**
 * @Author: Wizard
 * @Date: 2020/6/20 16:30
 */
//双亲委派机制的意义主要是保护一些基本类不受影响。
//比如常用的 String类， 其全限定名是 java.lang.String， 只是 java.lang 这个包下的类在使用的时候，可以不用 import 而直接使用。
//像这种基本类 按照双亲委派机制 都应该从 rt.jar 里去获取，而不应该从自定义加载器里去获取某个开发人员自己写的 java.lang.String,
// 毕竟开发人员自己写的 java.lang.String 可能有很多 bug,
// 通过这种方式，无论如何大家使用的都是 rt.jar 里的 java.lang.String 类了。
public class Kayleh {
    public void hello() {
        System.out.println("hello, this is Kayleh saying \"Hello!\"");
    }
}
package cn.kayleh.diyTomcat;

import cn.kayleh.diyTomcat.catalina.Server;

/**
 * @Author: Wizard
 * @Date: 2020/6/7 20:41
 */
public class Bootstrap {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}

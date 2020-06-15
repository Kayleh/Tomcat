package cn.kayleh.diyTomcat.catalina;

import cn.kayleh.diyTomcat.util.ServerXMLUtil;

/**
 * @Author: Wizard
 * @Date: 2020/6/15 14:13
 */
//一个 Service 下通常只有一个 Engine, 就不做成 List<Engine> 集合了。
public class Service {
    private String name;
    private Engine engine;

    public Service() {
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
    }

    public Engine getEngine() {
        return engine;
    }
}

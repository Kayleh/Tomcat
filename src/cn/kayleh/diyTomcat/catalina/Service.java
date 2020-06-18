package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.ServerXMLUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/6/15 14:13
 */
//一个 Service 下通常只有一个 Engine, 就不做成 List<Engine> 集合了。
public class Service {
    private String name;
    private Engine engine;
    private Server server;

    private List<Connector> connectors;

    public Service(Server server) {
        this.server = server;
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
        this.connectors = ServerXMLUtil.getConnector(this);
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }

    public void start() {
        init();
    }

    private void init() {
        TimeInterval timeInterval = DateUtil.timer();
        for (Connector connector : connectors) {
            connector.init();
        }
        LogFactory.get().info("Initialization processed in {} ms", timeInterval.intervalMs());
        for (Connector connector : connectors) {
            connector.start();
        }
    }

}

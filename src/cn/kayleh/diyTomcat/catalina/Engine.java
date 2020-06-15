package cn.kayleh.diyTomcat.catalina;

import cn.kayleh.diyTomcat.util.ServerXMLUtil;

import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/6/14 15:41
 * <p>
 * Engine 表示Servlet引擎，用来处理Servlet的请求
 */
public class Engine {
    //默认的 host 名称
    private String defaultHost;
    //Host 集合
    private List<Host> hosts;

    private Service service;

    public Engine(Service service) {
        this.service = service;
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();
        this.hosts = ServerXMLUtil.getHosts(this);
        checkDefault();
    }

    //判断默认的是否存在，否则就会抛出异常
    private void checkDefault() {
        if (null == getDefaultHost())
            throw new RuntimeException("the defaultHost" + defaultHost + " does not exist!");
    }

    //获取默认的Host对象
    public Host getDefaultHost() {
        for (Host host : hosts) {
            if (host.getName().equals(defaultHost))
                return host;
        }
        return null;
    }
}

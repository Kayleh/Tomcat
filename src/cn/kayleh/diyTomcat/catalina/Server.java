package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.http.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Wizard
 * @Date: 2020/6/15 14:38
 */
public class Server {
    private Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        TimeInterval timeInterval = DateUtil.timer();
        logJVM();
        init();
        LogFactory.get().info("Server startup in {} ms", timeInterval.intervalMs());
    }

    private void init() {
        service.start();
    }

    //像 tomcat 那样 一开始打印 jvm 信息
    private static void logJVM() {
        //这里获取日志对象的方式是 LogFactory.get() ，这种方式很方便，否则就要在每个类里面写成
        //static Logger logger = Logger.getLogger(XXX.class)
        //每个类都要写，是很繁琐的，所以我很喜欢 Hutool 这种获取日志的方式。
        //logJVM 会把 jvm 信息都打印出来，看上去就是如图所示的样子了。
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "Kayleh DiyTomcat/1.0.1");
        infos.put("Server built", "2020-06-11 15:55:01");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + ":\t\t" + infos.get(key));
        }
    }

}

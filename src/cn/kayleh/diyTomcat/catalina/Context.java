package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;

/**
 * @Author: Wizard
 * @Date: 2020/6/12 15:56
 */
//代表一个应用
public class Context {
    //path 表示访问的路径
    //docBase 表示对应在文件系统中的位置
    private String path;
    private String docBase;

    public Context(String path, String docBase) {
        TimeInterval timeInterval = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        //在构造方法中打上日志
        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }
}

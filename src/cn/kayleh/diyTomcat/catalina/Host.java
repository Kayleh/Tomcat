package cn.kayleh.diyTomcat.catalina;

import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.ServerXMLUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wizard
 * @Date: 2020/6/14 14:24
 */
public class Host {
    //name 表示名称。
    //contextMap 其实就是本来在 bootstrap 里的 contextMap , 只不过挪到这里来了。
    private String name;
    private Map<String, Context> contextMap;

    public Host() {
        this.name = ServerXMLUtil.getHostName();
        this.contextMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void scanContextsInServerXml() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    private void loadContextOnWebAppsFolder() {
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory()) continue;
            loadContext(folder);
        }
    }

    private void loadContext(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);
        contextMap.put(context.getPath(), context);
    }

    //提供 getContext 用于通过 path 获取 Context 对象
    public Context getContext(String path) {
        return contextMap.get(path);
    }


}

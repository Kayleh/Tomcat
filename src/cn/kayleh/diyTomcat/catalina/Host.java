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
    private String name;
    ////声明一个 contextMap 用于存放路径和Context 的映射。
    private Map<String, Context> contextMap;
    private Engine engine;

    public Host(String name, Engine engine) {
        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;

        scanContextsOnWebAppsFolder();
        scanContextsInServerXML();

    }

    //创建scanContextsInServerXML， 通过 ServerXMLUtil 获取 context, 放进 contextMap里。
    private void scanContextsInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts();
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    //创建 scanContextsOnWebAppsFolder 方法，用于扫描 webapps 文件夹下的目录，对这些目录调用 loadContext 进行加载。
    private void scanContextsOnWebAppsFolder() {
        //列出webapps下的每一个文件夹
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory()) continue;
            loadContext(folder);
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //加载这个目录成为 Context 对象。
    //如果是 ROOT，那么path 就是 "/", 如果是 a, 那么path 就是 "/a", 然后根据 path 和 它们所处于的路径创建 Context 对象。
    //然后把这些对象保存进 contextMap，方便后续使用。
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

package cn.kayleh.diyTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.classloader.WebappClassLoader;
import cn.kayleh.diyTomcat.exception.WebConfigDuplicatedException;
import cn.kayleh.diyTomcat.http.ApplicationContext;
import cn.kayleh.diyTomcat.http.StandardServletConfig;
import cn.kayleh.diyTomcat.util.ContextXMLUtil;
import cn.kayleh.diyTomcat.watcher.ContextFileChangeWatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Context 用来存放 Servlet 的映射信息
 *
 * @Author: Wizard
 * @Date: 2020/6/12 15:56
 */


//代表一个应用
public class Context {
    //path 表示访问的路径
    //docBase 表示对应在文件系统中的位置
    private String path;
    private String docBase;

    private File contextWebXmlFile;

    //地址对应 Servlet 的类名
    private Map<String, String> url_serveltClassName;
    //地址对应 Servlet 的名称
    private Map<String, String> url_serveltName;
    //Servlet 的名称对应类名
    private Map<String, String> ServeltName_ClassName;
    //Servlet 类名对应名称
    private Map<String, String> ClassName_serveltName;

    private List<String> loadOnStartupServletClassNames;

    // 声明 servlet_className_init_params 用于存放初始化信息
    private Map<String, Map<String, String>> servlet_className_init_params;

    private WebappClassLoader webappClassLoader;

    private Host host;
    private boolean reloadable;
    private ContextFileChangeWatcher contextFileChangeWatcher;

    private ServletContext servletContext;

    private Map<Class<?>, HttpServlet> servletPool;

    /**
     * 在构造方法中初始化前面定义的属性，并且调用 deploy 方法。
     *
     * @param path
     * @param docBase
     */
    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();

        this.host = host;
        this.reloadable = reloadable;

        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());

        this.url_serveltClassName = new HashMap<>();
        this.url_serveltName = new HashMap<>();
        this.ServeltName_ClassName = new HashMap<>();
        this.ClassName_serveltName = new HashMap<>();
        this.loadOnStartupServletClassNames = new ArrayList<>();

        this.servlet_className_init_params = new HashMap<>();

        this.servletContext = new ApplicationContext(this);

        this.servletPool = new HashMap<>();

        //在构造方法中初始化它，这里的 Thread.currentThread().getContextClassLoader() 就可以获取到 Bootstrap
        // 里通过 Thread.currentThread().setContextClassLoader(commonClassLoader); 设置的 commonClassLoader.
        //然后 根据 Tomcat 类加载器体系 commonClassLoader 作为 WebappClassLoader 父类存在。
        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());
    }

    private void parseServletInitParams(Document document) {
        Elements servletClassNameElements = document.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty())
                continue;
            //存放初始化参数
            Map<String, String> initsParams = new HashMap<>();
            for (Element element : initElements) {
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initsParams.put(name, value);
            }
            servlet_className_init_params.put(servletClassName, initsParams);
        }
        System.out.println("class_name_init_params:" + servlet_className_init_params);
    }


    //提供 getServlet 方法，根据类对象来获取 servlet 对象。 让 servlet 对象放进池子之前做初始化
    public synchronized HttpServlet getServlet(Class<?> clazz)
            throws IllegalAccessException, InstantiationException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            ServletContext servletContext = this.getServletContext();
            String className = clazz.getName();
            String servletName = ClassName_serveltName.get(className);
            Map<String, String> initParams = servlet_className_init_params.get(className);
            ServletConfig servletConfig = new StandardServletConfig(servletContext, servletName, initParams);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    //parseLoadOnStartup 解析哪些类需要做自启动
    public void parseLoadOnStartup(Document document) {
        Elements elements = document.select("load-on-startup");
        for (Element element : elements) {
            String loadOnStartupServletClassName = element.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }

    //对这些类做自启动
    public void handleLoadOnStartup() {
        for (String loadOnStartupServletClassName : loadOnStartupServletClassNames) {
            try {
                Class<?> clazz = webappClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            } catch (InstantiationException | ServletException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    //用于销毁所有的 servlets
    public void destroyServlets() {
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }


    //停止方法，把 webappClassLoader 和 contextFileChangeWatcher 停止了
    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
        destroyServlets();
    }

    //重载方法，通过它的父对象来重载它
    public void reload() {
        host.reload(this);
    }


    /**
     * 创建一个 Deploy 方法， 调用 init, 并打印日志
     */
    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();
        init();
        //在deploy 方法中初始化contextFileChangeWatcher ，并启动

        if (reloadable) {
            //     bug ↓
//            ContextFileChangeWatcher contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
//            LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase, timeInterval.intervalMs());
        }
    }


    /**
     * 初始化方法
     * 先判断是否有 web.xml 文件，如果没有就返回了
     * 然后判断是否重复
     * 接着进行 web.xml 的解析
     */
    private void init() {
        if (!contextWebXmlFile.exists()) {
            return;
        }
        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }
        //没有重复 就 解析
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document document = Jsoup.parse(xml);
        parseServletMapping(document);

        parseServletInitParams(document);

        parseLoadOnStartup(document);
        handleLoadOnStartup();
    }


    //parseServletMapping方法，把这些信息从 web.xml 中解析出来
    private void parseServletMapping(Document document) {
        // url_ServletName
        Elements mappingurlElements = document.select("servlet-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String servletName = mappingurlElement.parent().select("servlet-name").first().text();
            url_serveltName.put(urlPattern, servletName);
        }
        // servletName_className / className_servletName
        Elements servletNameElements = document.select("servlet servlet-name");
        for (Element servletNameElement : servletNameElements) {
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            ServeltName_ClassName.put(servletName, servletClass);
            ClassName_serveltName.put(servletClass, servletName);
        }

        // url_servletClassName
        Set<String> urls = url_serveltName.keySet();
        for (String url : urls) {
            String servletName = url_serveltName.get(url);
            String servletClass = ServeltName_ClassName.get(servletName);
            url_serveltClassName.put(url, servletClass);
        }
    }

    private void checkDuplicated(Document document, String mapping, String desc) throws WebConfigDuplicatedException {

        Elements elements = document.select(mapping);
        // 判断逻辑是放入一个集合，然后把集合排序之后看两临两个元素是否相同
        List<String> contents = new ArrayList<>();
        for (Element element : elements) {
            contents.add(element.text());
        }
        Collections.sort(contents);
        for (int i = 0; i < contents.size() - 1; i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void checkDuplicated() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document document = Jsoup.parse(xml);

        checkDuplicated(document, "servlet-mapping url-pattern", "servlet url 重复,请保持其唯一性:{} ");
        checkDuplicated(document, "servlet servlet-name", "servlet 名称重复,请保持其唯一性:{} ");
        checkDuplicated(document, "servlet servlet-class", "servlet 类名重复,请保持其唯一性:{} ");
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    //一个Web应用，应该有一个自己独立的 WebappClassLoader ， 所以在Context 里加上 webappClassLoader 属性，以及一个getter
    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    //通过 uri 获取Servlet 类名
    public String getServletClassName(String uri) {
        return url_serveltClassName.get(uri);
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

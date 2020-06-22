package cn.kayleh.diyTomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Wizard
 * @Date: 2020/6/22 9:04
 */
public class StandardServletConfig implements ServletConfig {
    private ServletContext servletContext;
    Map<String, String> initParameters;
    private String servletName;

    public StandardServletConfig(ServletContext servletContext, String servletName,
                                 Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.servletName = servletName;
        if (null == initParameters) {
            this.initParameters = new HashMap<>();
        }

    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

}

package cn.kayleh.diyTomcat.http;

import cn.kayleh.diyTomcat.catalina.Context;

import java.io.File;
import java.util.*;

/**
 * @Author: Wizard
 * @Date: 2020/6/21 21:21
 */
public class ApplicationContext extends BaseServletContext {

    private Map<String, Object> attributesMap;
    private Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;

    }

    @Override
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }

    @Override
    public Object getAttribute(String s) {
        return super.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributesMap.put(name,value);
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }
}

package cn.kayleh.diyTomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.kayleh.diyTomcat.catalina.Context;
import cn.kayleh.diyTomcat.classloader.JspClassLoader;
import cn.kayleh.diyTomcat.http.Request;
import cn.kayleh.diyTomcat.http.Response;
import cn.kayleh.diyTomcat.util.Constant;
import cn.kayleh.diyTomcat.util.JspUtil;
import cn.kayleh.diyTomcat.util.WebXMLUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: Wizard
 * @Date: 2020/6/24 17:29
 */
public class JspServlet extends HttpServlet {
    private static final long serialVerisonUID = 1L;
    private static JspServlet instance = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return instance;
    }

    public JspServlet() {
    }

    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();

            if ("/".equals(uri))
                uri = WebXMLUtil.getWelcomeFile(request.getContext());
            //去掉前缀/
            String fileName = StrUtil.removePrefix(uri, "/");
            // 获取JSp文件的全路径
            File file = FileUtil.file(request.getRealPath(fileName));
            // jsp文件的绝对路径
            File jspFile = file;
            if (jspFile.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                //subFolder 这个变量是用于处理 ROOT的，对于ROOT 这个 webapp 而言，
                // 它的 path 是 "/", 那么在 work 目录下，对应的应用目录就是 "_"。
                String subFolder;
                if ("/".equals(path)) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, '/', false);
                }
                //然后通过 JspUtil 获取 servlet 路径，看看是否存在。
                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (jspServletClassFile.exists()) {
                    //如果存在，再看看最后修改时间与 jsp 文件的最后修改时间 谁早谁晚。
                    JspUtil.compileJsp(context, jspFile);
                } else if (jspFile.lastModified() > jspServletClassFile.lastModified()) {
                    //当发现 jsp 更新之后，就会调用 invalidJspClassLoader 是指与之前的 JspClassLoader 脱钩。
                    JspUtil.compileJsp(context, jspFile);
                    JspClassLoader.invalidJspClassLoader(uri, context);
                }

                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                //根据uri 和 context 获取当前jsp 对应的 JspClassLoader
                //获取 jsp 对应的 servlet Class Name
                //通过 JspClassLoader 根据 servlet Class Name 加载类对象：jspServletClass
                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);
                Class jspServletClass = jspClassLoader.loadClass(jspServletClassName);

                //使用 context 现成的用于进行单例管理的 getServlet 方法获取 servlet 实例，
                // 然后调用其 service 方法。 最后设置 状态码为200.
                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request, response);

//                byte[] bytes = FileUtil.readBytes(file);
//                response.setBody(bytes);
                response.setStatus(Constant.CODE_200);

            } else {
                response.setStatus(Constant.CODE_404);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

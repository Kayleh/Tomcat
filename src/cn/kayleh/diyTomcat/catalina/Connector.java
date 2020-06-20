package cn.kayleh.diyTomcat.catalina;

import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.util.ThreadPoolUtil;
import cn.kayleh.diyTomcat.http.Request;
import cn.kayleh.diyTomcat.http.Response;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: Wizard
 * @Date: 2020/6/18 14:31
 */
public class Connector implements Runnable {
    int port;
    private Service service;

    public Connector(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //在端口8888上启动ServerSocket。服务端和浏览器通信是通过Socket进行通信的，所以这里需要启动一个 ServerSocket。
            //套了一层循环，处理掉一个Socket链接请求之后，再处理下一个链接请求。
            while (true) {
                //表示收到一个浏览器客户端的请求
                Socket accept = serverSocket.accept();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(accept, service);
                            Response response = new Response();
                            HttpProcessor httpProcessor = new HttpProcessor();
                            httpProcessor.execute(accept, request, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (!accept.isClosed())
                                try {
                                    accept.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                };
                ThreadPoolUtil.run(runnable);
            }
        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]", port);
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]", port);
        new Thread(this).start();
    }

}

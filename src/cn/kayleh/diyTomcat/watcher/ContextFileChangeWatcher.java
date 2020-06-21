package cn.kayleh.diyTomcat.watcher;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import cn.kayleh.diyTomcat.catalina.Context;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * Context 文件改变监听器：ContextFileChangeWatcher.
 *
 * @Author: Wizard
 * @Date: 2020/6/21 16:38
 */
public class ContextFileChangeWatcher {

    //monitor 是真正其作用的监听器。
    private WatchMonitor monitor;

    //stop 标记是否已经暂停。
    private boolean stop = false;

    //ContextFileChangeWatcher 构造方法带上 Context 对象，方便后续重载
    public ContextFileChangeWatcher(Context context) {
        /**
         通过WatchUtil.createAll 创建 监听器。
         context.getDocBase() 代表监听的文件夹
         Integer.MAX_VALUE 代表监听的深入，如果是0或者1，就表示只监听当前目录，而不监听子目录
         new Watcher 当有文件发生变化，那么就会访问 Watcher 对应的方法
         */
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {
            /**
             * 首先加上 synchronized 同步。因为这是一个异步处理的，当文件发生变化，会发过来很多次事件。
             * 所以我们得一个一个事件的处理，否则搞不好就会让 Context 重载多次。
             * @param event
             */
            private void dealWith(WatchEvent<?> event) {
                synchronized (ContextFileChangeWatcher.class) {
                    //取得当前发生变化的文件或者文件夹名称
                    String fileName = event.context().toString();
                    //当 stop 的时候，就表示已经重载过了，后面再来的消息就别搭理了。
                    if (stop)
                        return;
                    // 表示只应对 jar class 和 xml 发生的变化，其他的不需要重启，比如 html ,txt等，没必要重启
                    if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")) {
                        //标记一下，后续消息就别处理了
                        stop = true;
                        //打印下日志
                        LogFactory.get().info(ContextFileChangeWatcher.this + " 检测到了Web应用下的重要文件变化 {} ", fileName);
                        //进行重载
                        context.reload();
                    }
                }
            }

            /**
             *
             watcher 声明的方法，就是当文件发生创建，修改，删除 和 出错的时候。 所谓的出错比如文件不能删除，磁盘错误等等。
             这些方法，都归置归置，放进了 dealWith里。
             * @param event
             * @param currentPath
             */
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }
        });
        //守护线程，其实可设可不设
        this.monitor.setDaemon(true);
    }

    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.close();
    }
}

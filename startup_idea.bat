del /q bootstrap.jar
jar cvf0 bootstrap.jar -C out/production/diyTomcat cn/kayleh/diyTomcat/Bootstrap.class -C out/production/diyTomcat cn/kayleh/diyTomcat/classloader/CommonClassLoader.class
del /q lib/diyTomcat.jar
cd out
cd production
cd diyTomcat
jar cvf0 ../../../lib/diyTomcat.jar *
cd ..
cd ..
cd ..
java -cp bootstrap.jar cn.kayleh.diyTomcat.Bootstrap
pause
创建之前，先删除 bootstrap.jar
/q 表示不会弹出是否要删除的提示
把 bin 目录下的 Bootstrap 类和 CommonClassLoader 类打入到 bootstrap.jar 包。因为启动只需要这两个类就足够了，也就是只有这两个类的类加载器是 AppClassLoader, 其余的类加载器都应该是 CommonClassLoader。
解释下参数 cvf0
c 表示创建文档
v 表示显示明细
f 表示指定jar 的文件名
0 表示不压缩
创建之前，先删除 lib 目录下的 diytomcat, 我们项目的所有类，都放在这个 diytomcat.jar 里，以供运行
切换目录
把 diytomcat 目录下所有的类和资源都打包进 lib/diytomcat这里
返回项目目录
启动 Bootstrap 类
-cp bootstrap.jar 是指以 bootstrap.jar 里的类为依赖，启动 Bootstrap类
暂停一下的目的是。。。。如果启动失败了，多半是一位内端口号被占用了，倘若不暂停的话，就会屏幕一闪而过，暂停的目的是为了让同学们清楚地看到为什么启动会失败。
最后双击运行，可以看到图如所示的最后一行， Server 类的类加载器名称就是我们设计的 CommonClassLoader了
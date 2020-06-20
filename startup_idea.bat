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
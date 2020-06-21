# Tomcat
##### _**项目结构图**_
[项目结构图](http://localhost:8888/tomcat.png "标题")
![https://stepimagewm.how2j.cn/10584.png]()

```
热加载的流程图：
1. 首先创建 Context
2. 创建 Context 之后，创建个专属的监听器，用于监听当前 docBase 下文件的变化
3. 开始持续进行监听中
4. 判断发生变化的文件的后缀名
4.1 如果不是 class, jar 或者 xml 就不管它，继续监听
4.2 如果是，那么就先关闭监听，然后重载Context, 并且刷新 Host 里面的 contextMap
5. 重载其实就是重新创建一个新的 Context, 就又回到 1了
```
![热部署](https://stepimagewm.how2j.cn/10798.png)
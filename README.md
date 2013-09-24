Rupicsapp
=========

An image viewer whose images are issued by backend server

图片浏览器，使用分类-》专辑-》专辑图片三级展示方式，数据源来自后端服务器(使用Play框架实现):
[rupicsweb](../../../rupicsweb)

## 使用说明：

* 1.运行后台服务器，也可以选择发布到heroku
* 2.根据服务器的运行地址，修改RemoteServiceImpl.java中的访问地址：
```java
public static IRemoteService getInstance() {
		if (service == null) {

			 service = new RemoteServiceImpl("http://[server.ip]:9000/service");
		}
		return service;
	}
```
替换[server.ip]为真实ip，如果需要自行修改端口9000
* 3.编译运行手机APP，此时没有图片显示，需要自行登录服务器后台增加图片源及专辑

## Bug Report:

* [issues](../../issues)
* E-mail: vncntkarl2 At gmail.com

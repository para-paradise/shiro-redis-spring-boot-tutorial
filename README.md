# shiro-redis-spring-boot-tutorial

## 描述

该仓库是文章《redis未授权到shiro反序列化之session回显⻢》复现环境。

文章发布于：NOSEC安全讯息平台 - ⽩帽汇安全研究院 https://nosec.org/home/detail/5041.html

## 使用方式
项目maven打包后，运行环境启动类 ShiroRedisSpringBootTutorialApplication

启动redis：将 run.bat 放于 Redis-x64-3.2.100.zip 解压目录内运行。

注入Session Echo Shell：运行 InjectSessionEchoShell main方法
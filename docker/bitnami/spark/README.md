# 创建网络(Spark 专用)
```shell script
docker network create spark_net --driver bridge
```
# 启动 spark 容器
```shell script
docker-compose -f docker-compose.yaml up -d

/opt/bitnami/spark$ spark-shell --version
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.2.1
      /_/
                        
Using Scala version 2.12.15, OpenJDK 64-Bit Server VM, 1.8.0_322
Branch HEAD
Compiled by user hgao on 2022-01-20T19:26:14Z
Revision 4f25b3f71238a00508a356591553f2dfa89f8290
Url https://github.com/apache/spark
Type --help for more information.
```
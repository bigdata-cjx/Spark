# 部署 Spark
```
docker pull singularities/spark
docker-compose up -d 
```
## 查看
### 容器
```
docker-compose ps

     Name                  Command            State                                                                               Ports                                                                            
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
docker_master_1   start-spark master          Up      10020/tcp, 13562/tcp, 14000/tcp, 19888/tcp, 50010/tcp, 50020/tcp, 0.0.0.0:50070->50070/tcp,:::50070->50070/tcp, 50075/tcp, 50090/tcp, 50470/tcp, 50475/tcp,  
                                                      0.0.0.0:6066->6066/tcp,:::6066->6066/tcp, 0.0.0.0:7070->7070/tcp,:::7070->7070/tcp, 7077/tcp, 8020/tcp, 0.0.0.0:8080->8080/tcp,:::8080->8080/tcp, 8081/tcp,  
                                                      9000/tcp                                                                                                                                                     
docker_worker_1   start-spark worker master   Up      10020/tcp, 13562/tcp, 14000/tcp, 19888/tcp, 50010/tcp, 50020/tcp, 50070/tcp, 50075/tcp, 50090/tcp, 50470/tcp, 50475/tcp, 6066/tcp, 7077/tcp, 8020/tcp,       
                                                      8080/tcp, 8081/tcp, 9000/tcp
```
- 50070：HDFSwebUI的端口号
- 9000：非高可用访问数rpc端口
- 8080：sparkwebUI的端口号
- 8081：worker的webUI的端口号
- 7077：spark基于standalone的提交任务的端口号
### webUI
- HDFS: http://localhost:50070/dfshealth.html#tab-overview
- Spark: http://localhost:8080/
### 进入 master 容器查看各个组件的版本
```
# docker exec -it docker_master_1 /bin/bash
root@master:/# hadoop version
Hadoop 2.8.2
Subversion https://git-wip-us.apache.org/repos/asf/hadoop.git -r 66c47f2a01ad9637879e95f80c41f798373828fb
Compiled by jdu on 2017-10-19T20:39Z
Compiled with protoc 2.5.0
From source with checksum dce55e5afe30c210816b39b631a53b1d
This command was run using /usr/local/hadoop-2.8.2/share/hadoop/common/hadoop-common-2.8.2.jar
root@master:/# which is hadoop
/usr/local/hadoop-2.8.2/bin/hadoop

root@master:/# spark-shell
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
22/03/09 07:50:12 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Spark context Web UI available at http://172.19.0.2:4040
Spark context available as 'sc' (master = local[*], app id = local-1646812213317).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.2.1
      /_/
         
Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_151)
Type in expressions to have them evaluated.
Type :help for more information.

scala> 
```
- Hadoop 2.8.2
- Saprk 2.2.1
- Scala 2.11.8
- Java 1.8.0_151
# 测试
## Hadoop
查看 Hadoop 文件目录
```
# docker exec -it docker_master_1 /bin/bash
root@master:/# which is hadoop
/usr/local/hadoop-2.8.2/bin/hadoop
```
准备数据，上传到 HDFS
```
root@master:/# echo "spark hadoop hadoop java scala scala spark" > word_count.txt
root@master:/# cat word_count.txt 
spark hadoop hadoop java scala scala spark
root@master:/# echo "spark hadoop hadoop java scala scala spark" > word_count.txt
root@master:/# cat word_count.txt 
spark hadoop hadoop java scala scala spark
root@master:/# /usr/local/hadoop-2.8.2/bin/hdfs dfs -mkdir -p /user/data/
root@master:/# /usr/local/hadoop-2.8.2/bin/hdfs dfs -put ./word_count.txt /user/data/
root@master:/# /usr/local/hadoop-2.8.2/bin/hdfs dfs -text /user/data/word_count.txt
spark hadoop hadoop java scala scala spark
```
## Yarn
WordCount
```
root@master:/# /usr/local/hadoop-2.8.2/bin/hdfs dfs -mkdir -p /user/data/output/
root@master:/# /usr/local/hadoop-2.8.2/bin/yarn jar /usr/local/hadoop-2.8.2/share/hadoop/mapreduce/hadoop-mapreduce-examples-2.8.2.jar wordcount /user/data/word_count.txt /user/data/output/1
root@master:/# /usr/local/hadoop-2.8.2/bin/hdfs dfs -text /user/data/output/1/par*
hadoop	2
java	1
scala	2
spark	2
```
## Spark
WordCount
```
root@master:/# spark-shell
scala> val rdd = spark.read.textFile("hdfs://master:8020/user/data/word_count.txt")
rdd: org.apache.spark.sql.Dataset[String] = [value: string]

scala> rdd.flatMap(_.split(" ")).map((_,1)).groupBy("_1").count.show
+------+-----+
|    _1|count|
+------+-----+
| scala|    2|
| spark|    2|
|  java|    1|
|hadoop|    2|
+------+-----+
```
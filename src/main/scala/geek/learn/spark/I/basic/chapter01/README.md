# 导入数据，并进入容器开启spark-shell
```
➜  ~ docker cp /data/git-projects/bigdata-cjx/Spark/geek/learn-spark/I-basic/chapter01/wikiOfSpark.txt spark-master01:/opt/bitnami/spark/data/
➜  ~ docker exec -it spark-master01 /bin/bash                                                                                                 
I have no name!@spark-master01:/opt/bitnami/spark$ head data/wikiOfSpark.txt 
Apache Spark
From Wikipedia, the free encyclopedia
Jump to navigationJump to search
Apache Spark
Spark Logo
Original author(s)	Matei Zaharia
Developer(s)	Apache Spark
Initial release	May 26, 2014; 6 years ago
Stable release
3.1.1 / March 2, 2021; 2 months ago
I have no name!@spark-master01:/opt/bitnami/spark$ spark-shell
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
22/04/22 04:14:27 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Spark context Web UI available at http://spark-master01:4040
Spark context available as 'sc' (master = local[*], app id = local-1650600868022).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.2.1
      /_/
         
Using Scala version 2.12.15 (OpenJDK 64-Bit Server VM, Java 1.8.0_322)
Type in expressions to have them evaluated.
Type :help for more information.
```
# word count 入门
```
import org.apache.spark.rdd.RDD
 
// 这里的下划线"_"是占位符，代表数据文件的根目录
val rootPath: String = "/opt/bitnami/spark/data"
val file: String = s"${rootPath}/wikiOfSpark.txt"
 
// 读取文件内容
val lineRDD: RDD[String] = spark.sparkContext.textFile(file) 

// 以行为单位做分词
val wordRDD: RDD[String] = lineRDD.flatMap(line => line.split(" ")) 

// 过滤掉空字符串
val cleanWordRDD: RDD[String] = wordRDD.filter(word => !word.equals(""))

// 把RDD元素转换为（Key，Value）的形式
val kvRDD: RDD[(String, Int)] = cleanWordRDD.map(word => (word, 1)) 

// 按照单词做分组计数
val wordCounts: RDD[(String, Int)] = kvRDD.reduceByKey((x, y) => x + y) 

// 打印词频最高的5个词汇
wordCounts.map{case (k, v) => (v, k)}.sortByKey(false).take(5)
res0: Array[(Int, String)] = Array((67,the), (63,Spark), (54,a), (51,and), (50,of))
```
一句话版本
```
scala> spark.sparkContext.textFile(file).flatMap(line => line.split(" ")).filter(word => !word.equals("")).map(word => (word, 1)).reduceByKey((x, y) => x + y).map{case (k, v) => (v, k)}.sortByKey(false).take(5)
res1: Array[(Int, String)] = Array((67,the), (63,Spark), (54,a), (51,and), (50,of))
```
一句话版本简化
```
spark.sparkContext.textFile(file).flatMap(_.split(" ")).filter(!_.equals("")).map((_, 1)).reduceByKey(_ + _).map(t => (t._2, t._1)).sortByKey(false).take(5)
```
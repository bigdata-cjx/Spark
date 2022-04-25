package geek.learn.spark.I.basic.chapter03

import java.security.MessageDigest

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object WordCountExample {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("WordCountExample")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc = spark.sparkContext

    // 创建 RDD
    // 通过 SparkContext.parallelize 在内部数据之上创建 RDD；
    //    val words: Array[String] = Array("Spark", "is", "cool")
    //    val rdd: RDD[String] = sc.parallelize(words)
    //    rdd.take(2)
    //      .foreach(
    //        println(_)
    //      )
    // parallelize API 的典型用法，是在“小数据”之上创建 RDD。

    // 通过 SparkContext.textFile 等 API 从外部数据创建 RDD。
    val rootPath: String = "./src/main/resources"
    val file: String = s"${rootPath}/wikiOfSpark.txt"
    // 读取文件内容
    val lineRDD: RDD[String] = spark.sparkContext.textFile(file)
    // 把普通RDD转换为Paired RDD
    val cleanWordRDD: RDD[String] = lineRDD
      .flatMap(_.split(" "))
      .filter(!_.equals(""))

    // 把RDD元素转换为（Key，Value）的形式
    // 定义映射函数f(定义如下的映射函数 f，我们就可以改写 Word Count 的计数逻辑，也就是把“Spark”这个单词的统计计数权重提高一倍：)
    //    def f(word: String): (String, Int) = {
    //      if (word.equals("Spark")) {
    //        return (word, 2)
    //      }
    //      (word, 1)
    //    }
    //    val kvRDD: RDD[(String, Int)] = cleanWordRDD.map(f)

    // mapPartitions：以数据分区为粒度的数据转换
    // 把 Word Count 的计数需求，从原来的对单词计数，改为对单词的哈希值计数
    //    val kvRDD: RDD[(String, Int)] = cleanWordRDD.mapPartitions { partition =>
    //      // 注意！这里是以数据分区为粒度，获取MD5对象实例
    //      val md5 = MessageDigest.getInstance("MD5")
    //      val newPartition = partition.map(word => {
    //        // 在处理每一条数据记录的时候，可以复用同一个Partition内的MD5对象
    //        (md5.digest(word.getBytes()).mkString, 1)
    //      })
    //      newPartition
    //    }
    //    val result = kvRDD.reduceByKey(_ + _)
    //      .map(t => (t._2, t._1))
    //      .sortByKey(false)
    //      .take(5)
    //    result.foreach(println(_))

    // flatMap：从元素到集合、再从集合到元素
    // 改变 Word Count 的计算逻辑，由原来统计单词的计数，改为统计相邻单词共现的次数
    // 以行为单位提取相邻单词
    val wordPairRDD: RDD[String] = lineRDD.flatMap(line => {
      // 将行转换为单词数组
      val words: Array[String] = line.split(" ")
      // 将单个单词数组，转换为相邻单词数组
      for (i <- 0 until words.length - 1) yield words(i) + "-" + words(i + 1)
    })
    //    val kvRDD: RDD[(String, Int)] = wordPairRDD.map((_, 1))
    //    val result = kvRDD.reduceByKey(_ + _)
    //      .map(t => (t._2, t._1))
    //      .sortByKey(false)
    //      .take(5)
    //    result.foreach(println(_))

    // filter：过滤 RDD
    // 仅保留有意义的词对元素，我们希望结合标点符号列表，对 wordPairRDD 进行过滤。
    // 定义特殊字符列表
    val list: List[String] = List("&", "|", "#", "^", "@")

    // 定义判定函数f
    def f(s: String): Boolean = {
      val words: Array[String] = s.split("-")
      if(words.length == 2){
        val b1: Boolean = list.contains(words(0))
        val b2: Boolean = list.contains(words(1))
        return !b1 && !b2 // 返回不在特殊字符列表中的词汇对
      }
      false
    }

    // 使用filter(f)对RDD进行过滤
    val cleanedPairRDD: RDD[String] = wordPairRDD.filter(f)
    val kvRDD: RDD[(String, Int)] = cleanedPairRDD.map((_, 1))
    val result = kvRDD.reduceByKey(_ + _)
      .map(t => (t._2, t._1))
      .sortByKey(false)
      .take(5)
    result.foreach(println(_))
  }

}
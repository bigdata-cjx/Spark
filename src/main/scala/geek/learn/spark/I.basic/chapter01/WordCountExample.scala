package  geek.learn.spark.I.basic.chapter01

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object WordCountExample{

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("WordCountExample")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 这里的下划线"_"是占位符，代表数据文件的根目录
    val rootPath: String = "./src/main/resources"
    val file: String = s"${rootPath}/wikiOfSpark.txt"

    val result: Array[(Int, String)] = spark.sparkContext
      .textFile(file)
      .flatMap(_.split(" "))
      .filter(!_.equals(""))
      .map((_, 1))
      .reduceByKey(_ + _)
      .map(t => (t._2, t._1))
      .sortByKey(false)
      .take(5)

    result.foreach(println(_))
  }

}
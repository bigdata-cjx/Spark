package geek.learn.spark.I.basic.chapter07

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/*
groupByKey、reduceByKey、aggregateByKey 和 sortByKey
这 4 个算子都会引入繁重的 Shuffle 计算
只能作用（Apply）在 Paired RDD 之上，所谓 Paired RDD，它指的是元素类型为（Key，Value）键值对的 RDD。
 */

object Demo {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster("local[3]")
      .setAppName("WordCountExample")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc = spark.sparkContext

    // 创建 RDD
    // 通过 SparkContext.parallelize 在内部数据之上创建 RDD；
    val words: Array[(String, String)] = Array(
      ("Frame", "Spark"), ("Frame", "Flink"), ("Frame", "Storm"),
      ("Language", "Scala"), ("Language", "Java"), ("Language", "Python"),
      ("Ecology", "Hadoop"), ("Ecology", "HBase"), ("Ecology", "Kafka"), ("Ecology", "Hive")
    )
    val rdd: RDD[(String, String)] = sc.parallelize(words)
    rdd.foreach(println(_))

    // groupByKey：分组收集 (groupByKey 会把 RDD 的类型，由 RDD[(Key, Value)]转换为 RDD[(Key, Value 集合)])
    //    val group: RDD[(String, Iterable[String])] = rdd.groupByKey()
    //    group.foreach(println(_))


    // reduceByKey：分组聚合 (计算逻辑，就是根据聚合函数 f 给出的算法，把 Key 值相同的多个元素，聚合成一个元素。)
    /*
    给定 RDD[(Key 类型，Value 类型)]，聚合函数 f 的类型，必须是（Value 类型，Value 类型） => （Value 类型）。
    换句话说，函数 f 的形参，必须是两个数值，且数值的类型必须与 Value 的类型相同，而 f 的返回值，也必须是 Value 类型的数值。
     */
    // value 拼接
    //    def f(x: String, y: String): String = {
    //       x + " - " + y
    //    }
    //    val reduce = rdd.reduceByKey(f)
    //    reduce.foreach(println(_))


    // aggregateByKey：更加灵活的聚合算子 (需要提供一个初始值，一个 Map 端聚合函数 f1，以及一个 Reduce 端聚合函数 f2)
    // value 的长度加和
    // 聚合函数 f1 获取每个 value 的长度
    //    def f1(x: Int, y: String): Int = {
    //      x + y.length
    //    }
    //
    //    // 聚合函数 f2 长度加和
    //    def f2(x: Int, y: Int): Int = {
    //      x + y
    //    }
    //
    //    val aggregate = rdd.aggregateByKey(0)(f1, f2)
    //    aggregate.foreach(println(_))


    // sortByKey：排序
    // 在自己分区内排序，如果要最终结果是正确的排序，需要将分区设置为1，或者 `collect` or `save` on the resulting RDD
    val kv: Array[(String, Int)] = Array(
      ("A", 3), ("C", 1), ("B", 2)
    )
    val kvRdd: RDD[(String, Int)] = sc.parallelize(kv)
    kvRdd.foreach(println(_))
    val sort = kvRdd.sortByKey(false, 1)
    //    val sort = kvRdd.sortByKey(false).collect()
    sort.foreach(println(_))
  }

}
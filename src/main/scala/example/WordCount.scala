package example

import scala.concurrent.duration.FiniteDuration

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

object WordCount {

  type WordCount = (String, Int)

  def countWords(
      ssc: StreamingContext,
      lines: DStream[String],
      windowDuration: FiniteDuration,
      slideDuration: FiniteDuration
  ): DStream[WordCount] = {

    import scala.language.implicitConversions
    implicit def finiteDurationToSparkDuration(value: FiniteDuration): Duration = Duration(value.toMillis)

    val sc = ssc.sparkContext

    val windowDurationVar = sc.broadcast(windowDuration)
    val slideDurationVar = sc.broadcast(slideDuration)

    val words = lines
      .transform(splitLine)
      .transform(skipEmptyWords)
      .transform(toLowerCase)

    val wordCounts = words
      .map(word => (word, 1))
      .reduceByKeyAndWindow(_ + _, _ - _, windowDurationVar.value, slideDurationVar.value)

    wordCounts
      .transform(skipEmptyWordCounts)
      .transform(sortWordCounts)
  }

  def toLowerCase: RDD[String] => RDD[String] =
    (words: RDD[String]) => words.map(word => word.toLowerCase)

  def splitLine: RDD[String] => RDD[String] =
    (lines: RDD[String]) => lines.flatMap(line => line.split("[^\\p{L}]"))

  def skipEmptyWords: RDD[String] => RDD[String] =
    (words: RDD[String]) => words.filter(word => !word.isEmpty)

  def skipEmptyWordCounts: RDD[(String, Int)] => RDD[(String, Int)] =
    (wordCounts: RDD[WordCount]) => wordCounts.filter(wordCount => wordCount._2 > 0)

  def sortWordCounts: RDD[(String, Int)] => RDD[(String, Int)] =
    (wordCounts: RDD[WordCount]) => wordCounts.sortByKey()

}

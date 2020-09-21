package example

import scala.concurrent.duration.FiniteDuration

import org.apache.spark.storage.StorageLevel
import controllers._

class WordCountJob(config: WordCountJobConfig, source: KafkaDStreamSource) extends SparkStreamingApplication {

  override def sparkConfig: Map[String, String] = config.spark

  override def streamingBatchDuration: FiniteDuration = config.streamingBatchDuration

  override def streamingCheckpointDir: String = config.streamingCheckpointDir

  def start(): Unit = {
    withSparkStreamingContext { (sc, ssc) =>
      val input = source.createSource(ssc, config.inputTopic)

      val stringCodec = sc.broadcast(KafkaPayloadStringCodec())
      val lines = input.flatMap(stringCodec.value.decodeValue(_))

      val countedWords = WordCount.countWords(
        ssc,
        lines,
        config.windowDuration,
        config.slideDuration
      )

      val output = countedWords
        .map(_.toString())
        .map(stringCodec.value.encodeValue(_))

      output.persist(StorageLevel.MEMORY_ONLY_SER)

      import KafkaDStreamSink._
      output.sendToKafka(config.sinkKafka, config.outputTopic)
    }
  }

}

object WordCountJob {

  def main(args: Array[String]): Unit = {
    val config = WordCountJobConfig()

    val streamingJob = new WordCountJob(config, KafkaDStreamSource(config.sourceKafka))
    streamingJob.start()
  }

}

case class WordCountJobConfig(
    inputTopic: String,
    outputTopic: String,
    windowDuration: FiniteDuration,
    slideDuration: FiniteDuration,
    spark: Map[String, String],
    streamingBatchDuration: FiniteDuration,
    streamingCheckpointDir: String,
    sourceKafka: Map[String, String],
    sinkKafka: Map[String, String]
) extends Serializable

object WordCountJobConfig {

  import com.typesafe.config.Config
  import com.typesafe.config.ConfigFactory
  import net.ceedubs.ficus.Ficus._

  def apply(): WordCountJobConfig = apply(ConfigFactory.load)

  def apply(applicationConfig: Config): WordCountJobConfig = {

    val config = applicationConfig.getConfig("wordCountJob")

    new WordCountJobConfig(
      config.as[String]("input.topic"),
      config.as[String]("output.topic"),
      config.as[FiniteDuration]("windowDuration"),
      config.as[FiniteDuration]("slideDuration"),
      config.as[Map[String, String]]("spark"),
      config.as[FiniteDuration]("streamingBatchDuration"),
      config.as[String]("streamingCheckpointDir"),
      config.as[Map[String, String]]("kafkaSource"),
      config.as[Map[String, String]]("kafkaSink")
    )
  }

}

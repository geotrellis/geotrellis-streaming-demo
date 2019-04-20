package com.azavea.conf

import org.apache.spark.streaming.{Duration, Seconds}

case class KafkaStreamConfig(threads: Int, topic: String, otopic: String, applicationId: String, bootstrapServers: String)
case class SparkStreamConfig(
  batchDuration: Int,
  autoOffsetReset: String,
  autoCommit: Boolean,
  groupId: String,
  publishToKafka: Boolean,
  checkpointDir: Option[String] = None,
  partitions: Option[Int] = None
) {
  def duration: Duration = Seconds(batchDuration)
}
case class IngestStreamConfig(kafka: KafkaStreamConfig, spark: SparkStreamConfig)

object IngestStreamConfig {
  lazy val conf: IngestStreamConfig = pureconfig.loadConfigOrThrow[IngestStreamConfig]("ingest.stream")
  implicit def IngestStreamConfigObjectToClass(obj: IngestStreamConfig.type): IngestStreamConfig = conf
}

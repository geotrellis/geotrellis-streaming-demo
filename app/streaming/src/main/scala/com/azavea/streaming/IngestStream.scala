/*
 * Copyright 2019 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azavea.streaming

import com.azavea.conf.IngestStreamConfig
import com.azavea.json._
import com.azavea.kafka._

import io.circe.syntax._
import geotrellis.spark._
import geotrellis.spark.io.hadoop.SerializableConfiguration
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Duration, StreamingContext}
import com.typesafe.scalalogging.LazyLogging

case class IngestStream(
  sparkConf: SparkConf = IngestStream.sparkConfig,
  topic: String = IngestStreamConfig.kafka.topic,
  otopic: String = IngestStreamConfig.kafka.otopic,
  batchDuration: Duration = IngestStreamConfig.spark.duration,
  kafkaParams: Map[String, Object] = IngestStream.DEFAULT_CONFIG,
  publishToKafka: Boolean = IngestStreamConfig.spark.publishToKafka,
  partitionsNumber: Option[Int] = IngestStreamConfig.spark.partitions,
  checkpointDir: Option[String] = IngestStreamConfig.spark.checkpointDir
) extends LazyLogging {
  val bootstrapServers: String = kafkaParams("bootstrap.servers").asInstanceOf[String]

  @transient val ssc = new StreamingContext(sparkConf, batchDuration)
  checkpointDir.filter(_.nonEmpty).foreach(ssc.checkpoint)

  implicit val conf: SerializableConfiguration = SerializableConfiguration(ssc.sparkContext.hadoopConfiguration)

  // Create direct kafka stream with brokers and topics
  val topicsSet: Set[String] = Set(topic)
  @transient val stream: InputDStream[ConsumerRecord[String, String]] =
    KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams)
    )

  // parsed DStream
  @transient val parsedStream: DStream[Fields] =
    stream
      .flatMap { record =>
        logger.info(s"fields before parsing:: ${record.value}")
        val result = Fields.fromString(record.value)
        result.foreach { fields => logger.info(s"fields after parsing:: ${fields}:") }
        result
      }

  // foreach triggers stream execution
  // if no partitions number is passed number of partitions would be equal to the number of kafka topics
  partitionsNumber
    .fold(parsedStream)(parsedStream.repartition)
    .foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        val sender = getMessageSender(bootstrapServers)
        publishToKafka(partition.flatMap(ProcessStream(_)), sender)
      }
    }


  def start: Unit = ssc.start()

  def await: Unit = ssc.awaitTermination()

  def run: Unit = { start; await }

  def publishToKafka(stats: PolygonalStatsDouble, sender: MessageSender[String, String]): Unit =
    if (publishToKafka) {
      logger.info(s"publishing to kafka: ${stats.asJson.spaces2}")
      sender.batchWriteValue(otopic, stats.asJson.noSpaces :: Nil)
    }

  def publishToKafka(partition: Iterator[PolygonalStatsDouble], sender: MessageSender[String, String]): Unit =
    partition.foreach(publishToKafka(_, sender))
}

object IngestStream {
  val DEFAULT_CONFIG: Map[String, Object] = {
    Map(
      "bootstrap.servers"  -> IngestStreamConfig.kafka.bootstrapServers,
      "key.deserializer"   -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id"           -> IngestStreamConfig.conf.spark.groupId,
      "auto.offset.reset"  -> IngestStreamConfig.conf.spark.autoOffsetReset,
      "enable.auto.commit" -> (IngestStreamConfig.conf.spark.autoCommit: java.lang.Boolean)
    )
  }

  def sparkConfig: SparkConf =
    new SparkConf()
      .setAppName(IngestStreamConfig.kafka.applicationId)
      .setIfMissing("spark.master", "local[*]")
      .set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
      .set("spark.kryo.registrator", classOf[geotrellis.spark.io.kryo.KryoRegistrator].getName)
}

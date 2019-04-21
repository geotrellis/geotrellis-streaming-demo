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

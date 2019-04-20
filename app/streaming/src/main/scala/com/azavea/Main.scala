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

package com.azavea

import com.azavea.streaming.IngestStream
import com.monovore.decline._

object Main extends CommandApp(
  name = "geotrellis-streaming",
  header = "GeoTrellis SparkStreaming application",
  main = {
    val publishToKafka = Opts.option[String]("publishToKafka", help = "Publish or not to the output kafka topic").withDefault("true")
    publishToKafka.map { param => IngestStream(publishToKafka = param.toBoolean).run }
  }
)

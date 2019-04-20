package com.azavea

import com.azavea.streaming.IngestStream
import com.monovore.decline._

object Main extends CommandApp(
  name = "geotrellis-streaming",
  header = "GeoTrellis SparkStreaming application",
  main = {
    val publishToKafka = Opts.option[String]("publishToKafka", help = "Publish or not to the output kafka topic") .withDefault("true")
    publishToKafka.map { param =>
      val stream = IngestStream(publishToKafka = param.toBoolean)
      stream.start
      stream.await
    }
  }
)

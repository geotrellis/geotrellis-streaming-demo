package com.azavea.json

import geotrellis.raster.histogram.Histogram
import geotrellis.raster.summary.Statistics
import geotrellis.spark.io.hadoop._

import io.circe.syntax._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

@ConfiguredJsonCodec
case class PolygonalStatsDouble(
  uri: String,
  polygonalHistogramDouble: Array[Histogram[Double]],
  polygonalStatisticsDouble: Array[Statistics[Double]],
  polygonalMean: Array[Double],
  polygonalSumDouble: Array[Double],
  polygonalMinDouble: Array[Double],
  polygonalMaxDouble: Array[Double]
) {
  def write(path: Path, conf: Configuration): PolygonalStatsDouble = {
    HdfsUtils.write(path, conf) { _.write(this.asJson.spaces2.getBytes) }
    this
  }
}

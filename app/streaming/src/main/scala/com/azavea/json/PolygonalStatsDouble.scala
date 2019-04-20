package com.azavea.json

import geotrellis.raster.histogram.Histogram
import geotrellis.raster.summary.Statistics
import geotrellis.spark.io.hadoop._

import io.circe.syntax._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import java.io.PrintWriter

@ConfiguredJsonCodec
case class PolygonalStatsDouble(
  uri: String,
  polygonalHistogramDouble: Array[Histogram[Double]],
  polygonalStatisticsDouble: Array[Statistics[Double]],
  polygonalMean: Array[Double],
  polygonalSumDouble: Array[Double],
  polygonalMinDouble: Array[Double],
  polygonalMaxDouble: Array[Double]
) { self =>
  def write(path: Path, conf: Configuration): PolygonalStatsDouble = {
    HdfsUtils.write(path, conf) { _.write(self.asJson.spaces2.getBytes) }
    self
  }

  def write(path: String): PolygonalStatsDouble = {
    new PrintWriter(path) { write(self.asJson.spaces2); close }
    self
  }
}

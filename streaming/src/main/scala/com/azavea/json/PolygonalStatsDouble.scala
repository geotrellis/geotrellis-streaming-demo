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

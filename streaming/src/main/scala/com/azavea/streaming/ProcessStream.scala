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

import com.azavea._
import com.azavea.json.{Fields, PolygonalStatsDouble}

import geotrellis.spark.io.hadoop._
import geotrellis.contrib.vlm.RasterSource
import org.apache.hadoop.fs.Path

object ProcessStream {
  def apply(fields: Fields)(implicit conf: SerializableConfiguration): List[PolygonalStatsDouble] = {
    // reproject source
    val source = fields.targetCRS.fold(getRasterSource(fields.uri): RasterSource) { getRasterSource(fields.uri).reproject(_) }

    // reproject field into the source CRS
    fields
      .list
      .map(_.reproject(source.crs))
      .flatMap { field =>
        val outputTiffPath = s"${field.outputPath}/${field.id}.tiff"
        val outputJsonPath = s"${field.outputPath}/${field.id}.json"
        val result = source.read(field.polygon.envelope).map(_.mask(field.polygon))
        // calculate polygonal stats and persist both raster and generates stats as a JSON file

        result.map { raster =>
          val stats = raster.polygonalStatsDouble(outputTiffPath.toString)(field.polygon)
          if(field.outputPath.startsWith("s3://") || field.outputPath.startsWith("hdfs://")) {
            raster.toGeoTiff(source.crs).write(new Path(outputTiffPath), conf.value)
            stats.write(new Path(outputJsonPath), conf.value)
          } else {
            raster.toGeoTiff(source.crs).write(outputTiffPath)
            stats.write(new Path(outputJsonPath), conf.value)
          }
        }
      }
  }
}

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

import com.azavea._

import geotrellis.vector.MultiPolygon
import geotrellis.proj4.{CRS, LatLng}
import io.circe.parser._
import io.circe.generic.extras.ConfiguredJsonCodec
import com.typesafe.scalalogging.LazyLogging

// outputPath can be a folder, in this case polygon id would be the name of the output
@ConfiguredJsonCodec
case class Field(id: String, polygon: MultiPolygon, outputPath: String, crs: CRS = LatLng) {
  def reproject(targetCRS: CRS): Field = this.copy(
    polygon = polygon.reproject(crs, targetCRS),
    crs     = targetCRS
  )
}

@ConfiguredJsonCodec
case class Fields(name: String, uri: String, list: List[Field], targetCRS: Option[CRS] = None)

object Fields extends LazyLogging {
  def fromString(value: String): Option[Fields] = {
    parse(value) match {
      case Right(r) => r.as[Fields] match {
        case Right(r) => Some(r)
        case Left(e) =>
          logger.warn(e.stackTraceString)
          None
      }
      case Left(e) =>
        logger.warn(e.stackTraceString)
        None
    }
  }
}

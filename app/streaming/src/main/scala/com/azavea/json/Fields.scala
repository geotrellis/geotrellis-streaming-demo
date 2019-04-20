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
          println(e.stackTraceString)
          None
      }
      case Left(e) =>
        println(e.stackTraceString)
        None
    }
  }
}

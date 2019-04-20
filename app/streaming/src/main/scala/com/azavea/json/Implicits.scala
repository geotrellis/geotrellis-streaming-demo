package com.azavea.json

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

import geotrellis.vector._
import geotrellis.vector.io._
import geotrellis.raster.io._
import geotrellis.raster.histogram._
import geotrellis.raster.summary._
import geotrellis.proj4.CRS

import spray.json._
import cats.syntax.EitherOps
import cats.implicits._

import java.net.URI
import scala.util.Try

trait Implicits extends Serializable {
  implicit val config: Configuration = Configuration.default.withDefaults.withSnakeCaseMemberNames

  val prettyJsonPrinter: Printer = Printer.spaces2.copy(dropNullValues = true)
  implicit val uriEncoder: Encoder[URI] =
    Encoder.encodeString.contramap[URI] { _.toString }
  implicit val uriDecoder: Decoder[URI] =
    Decoder.decodeString.emap { str =>
      Either.catchNonFatal(new URI(str)).leftMap(_ => "URI")
    }

  implicit val extentEncoder: Encoder[Extent] =
    new Encoder[Extent] {
      final def apply(extent: Extent): Json =
        List(extent.xmin, extent.ymin, extent.xmax, extent.ymax).asJson
    }
  implicit val extentDecoder: Decoder[Extent] =
    Decoder[Json] emap { js =>
      new EitherOps(js.as[List[Double]]).map { case List(xmin, ymin, xmax, ymax) =>
        Extent(xmin, ymin, xmax, ymax)
      }.leftMap(_ => "Extent")
    }

  implicit val pointEncoder: Encoder[Point] =
    new Encoder[Point] {
      final def apply(p: Point): Json =
        Json.obj("lat" -> p.y.asJson, "lon" -> p.x.asJson)
    }
  implicit val pointDecoder: Decoder[Point] =
    Decoder.decodeJson.emap { json: Json =>
      val lat = json.hcursor.downField("lat").as[Double].toOption
      val lon = json.hcursor.downField("lon").as[Double].toOption
      val res = (lat, lon).mapN { case (y, x) => Point(x, y) }

      res.toRight[String]("Point"): Either[String, Point]
    }

  implicit val geometryEncoder: Encoder[Geometry] =
    new Encoder[Geometry] {
      final def apply(geom: Geometry): Json = {
        parse(geom.toGeoJson) match {
          case Right(js: Json) => js
          case Left(e) => throw e
        }
      }
    }

  implicit val geometryDecoder: Decoder[Geometry] = Decoder[Json] map {
    _.spaces4.parseGeoJson[Geometry]
  }

  implicit val multipolygonEncoder: Encoder[MultiPolygon] =
    new Encoder[MultiPolygon] {
      final def apply(mp: MultiPolygon): Json = {
        parse(mp.toGeoJson) match {
          case Right(js: Json) => js
          case Left(e) => throw e
        }
      }
    }

  implicit val multipolygonDecoder: Decoder[MultiPolygon] = Decoder[Json] map {
    _.spaces4.parseGeoJson[MultiPolygon]
  }

  implicit val polygonEncoder: Encoder[Polygon] =
    new Encoder[Polygon] {
      final def apply(p: Polygon): Json = {
        parse(p.toGeoJson) match {
          case Right(js: Json) => js
          case Left(e) => throw e
        }
      }
    }

  implicit val polygonDecoder: Decoder[Polygon] = Decoder[Json] map {
    _.spaces4.parseGeoJson[Polygon]
  }

  implicit val crsEncoder: Encoder[CRS] =
    Encoder.encodeString.contramap[CRS] { crs => crs.epsgCode.map { c => s"epsg:$c" }.getOrElse(crs.toProj4String) }
  implicit val crsDecoder: Decoder[CRS] =
    Decoder.decodeString.emap { str =>
      Either
        .catchNonFatal(Try(CRS.fromName(str)) getOrElse CRS.fromString(str))
        .leftMap(_ => "CRS")
    }

  implicit val encodeKeyDouble: KeyEncoder[Double] = new KeyEncoder[Double] {
    def apply(key: Double): String = key.toString
  }

  implicit val sprayJsonEncoder: Encoder[JsValue] = new Encoder[JsValue] {
    def apply(jsvalue: JsValue): Json =
      parse(jsvalue.compactPrint) match {
        case Right(success) => success
        case Left(fail)     => throw fail
      }
  }

  implicit val histogramDecoder: Decoder[Histogram[Double]] =
    Decoder[Json].map { js =>
      js.noSpaces.parseJson.convertTo[Histogram[Double]]
    }

  implicit val histogramEncoder: Encoder[Histogram[Double]] =
    new Encoder[Histogram[Double]] {
      def apply(hist: Histogram[Double]): Json = hist.toJson.asJson
    }

  implicit val statsDecoder: Decoder[Statistics[Double]] = deriveDecoder
  implicit val statsEncoder: Encoder[Statistics[Double]] = deriveEncoder
}

object Implicits extends Implicits

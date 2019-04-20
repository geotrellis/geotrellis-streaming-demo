package com

import com.azavea.json._
import com.azavea.conf.GDALEnabled

import geotrellis.contrib.vlm.RasterSource
import geotrellis.contrib.vlm.gdal.GDALRasterSource
import geotrellis.contrib.vlm.geotiff.GeoTiffRasterSource
import geotrellis.raster.histogram.Histogram
import geotrellis.raster.{MultibandTile, Raster}
import geotrellis.vector.MultiPolygon
import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.{GeoTiff, MultibandGeoTiff}

import java.io.{PrintWriter, StringWriter}

package object azavea {
  implicit class throwableExtensions[T <: Throwable](th: T) {
    def stackTraceString: String = {
      val writer = new StringWriter()
      th.printStackTrace(new PrintWriter(writer))
      writer.toString
    }
  }

  implicit class rasterMethods(val self: Raster[MultibandTile]) extends AnyVal {
    def polygonalHistogramDouble(multiPolygon: MultiPolygon): Array[Histogram[Double]] =
      self.tile.polygonalHistogramDouble(self.extent, multiPolygon)

    def polygonalMean(multiPolygon: MultiPolygon): Array[Double] =
      self.tile.polygonalMean(self.extent, multiPolygon)

    def polygonalSumDouble(multiPolygon: MultiPolygon): Array[Double] =
      self.tile.polygonalSumDouble(self.extent, multiPolygon)

    def polygonalMinDouble(multiPolygon: MultiPolygon): Array[Double] =
      self.tile.polygonalMinDouble(self.extent, multiPolygon)

    def polygonalMaxDouble(multiPolygon: MultiPolygon): Array[Double] =
      self.tile.polygonalMinDouble(self.extent, multiPolygon)

    def polygonalStatsDouble(uri: String)(multiPolygon: MultiPolygon): PolygonalStatsDouble = {
      val histogram = polygonalHistogramDouble(multiPolygon)
      PolygonalStatsDouble(
        uri                       = uri,
        polygonalHistogramDouble  = histogram,
        polygonalStatisticsDouble = histogram.flatMap(_.statistics),
        polygonalMean             = polygonalMean(multiPolygon),
        polygonalSumDouble        = polygonalSumDouble(multiPolygon),
        polygonalMinDouble        = polygonalMinDouble(multiPolygon),
        polygonalMaxDouble        = polygonalMaxDouble(multiPolygon)
      )
    }

    def toGeoTiff(crs: CRS): MultibandGeoTiff = GeoTiff(self, crs)
  }

  def getRasterSource(uri: String): RasterSource = if(GDALEnabled.enabled) GDALRasterSource(uri) else GeoTiffRasterSource(uri)
}

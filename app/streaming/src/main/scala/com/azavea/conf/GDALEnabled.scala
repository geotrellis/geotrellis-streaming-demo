package com.azavea.conf

case class GDALEnabled(enabled: Boolean = true)

object GDALEnabled {
  lazy val conf: GDALEnabled = pureconfig.loadConfigOrThrow[GDALEnabled]("vlm.source.gdal")
  implicit def GDALEnabledObjectToClass(obj: GDALEnabled.type): GDALEnabled = conf
}

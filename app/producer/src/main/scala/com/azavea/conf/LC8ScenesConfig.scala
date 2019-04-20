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

package com.azavea.conf

import geotrellis.proj4.CRS
import scala.util.Try

case class LC8Scene(name: String, band: Int, count: Int, crs: String, outputPath: String) {
  private val pattern = """_([0-9]{6})_""".r
  def path: String = {
    val (path, row) = pattern.findFirstIn(name).getOrElse(throw new Exception("Bad LC8 Scene name")).init.tail.splitAt(3)
    s"s3://landsat-pds/c1/L8/${path}/${row}/$name/${name}_B${band}.TIF"
  }

  def getCRS: CRS = Try(CRS.fromName(crs)) getOrElse CRS.fromString(crs)
}

case class LC8ScenesConfig(scenes: List[LC8Scene])

object LC8ScenesConfig {
  lazy val conf: LC8ScenesConfig = pureconfig.loadConfigOrThrow[LC8ScenesConfig]("lc8")
  implicit def LC8ScenesConfigObjectToClass(obj: LC8ScenesConfig.type): LC8ScenesConfig = conf
}

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

package com.azavea.generator

import com.azavea._
import com.azavea.kafka._
import com.azavea.streaming._
import com.azavea.conf._
import com.azavea.json._

import io.circe.syntax._
import geotrellis.vector._
import cats.effect._
import cats.implicits._

import scala.util._
import java.io.PrintWriter

object Generator {
  lazy val messageSender: MessageSender[String, String] = getMessageSender(IngestStreamConfig.kafka.bootstrapServers)

  def randomExtentWithin(extent: Extent, sampleScale: Double = 0.10): Extent = {
    assert(sampleScale > 0 && sampleScale <= 1)
    val extentWidth = extent.xmax - extent.xmin
    val extentHeight = extent.ymax - extent.ymin

    val sampleWidth = extentWidth * sampleScale
    val sampleHeight = extentHeight * sampleScale

    val testRandom = Random.nextDouble()
    val subsetXMin = (testRandom * (extentWidth - sampleWidth)) + extent.xmin
    val subsetYMin = (Random.nextDouble() * (extentHeight - sampleHeight)) + extent.ymin

    Extent(subsetXMin, subsetYMin, subsetXMin + sampleWidth, subsetYMin + sampleHeight)
  }

  def multiPolygons(extent: Extent)(count: Int): Seq[(Int, MultiPolygon)] = {
    val polygons =
      for {
        id <- 1 to count
        polygon = randomExtentWithin(extent).toPolygon
      } yield (id, MultiPolygon(polygon))

    if(polygons.count(_._2.intersects(extent.toPolygon)) == count) polygons
    else multiPolygons(extent)(count)
  }

  // NOTE: pretty print is done only for demo purposes
  // for a real kafka instance it's recommended to
  def persistFields(fields: Fields, path: String): Unit =
    if(path.isLocalPath) new PrintWriter(s"$path/${fields.name}.json") { write(fields.asJson.spaces2); close }
    else throw new Exception("Only local FS is supported now")

  def sendFields(fields: Fields, path: String): Unit =
    messageSender.batchWriteValue(IngestStreamConfig.kafka.topic, fields.asJson.spaces2 :: Nil)

  def sendPersisted(path: String): Unit =
    messageSender.batchWriteValue(IngestStreamConfig.kafka.topic, getListOfFiles(path).map(getJsonFromFile).filter(_.nonEmpty))

  def generateFields(implicit cs: ContextShift[IO]): List[IO[Fields]] =
    LC8ScenesConfig.scenes.map { scene =>
      IO {
        val source = getRasterSource(scene.path)
        val extent = source.extent
        val crs = source.crs
        val fields =
          multiPolygons(extent)(scene.count)
            .map { case (id, mp) =>
              Field(
                id         = s"${scene.name}-${id}",
                polygon    = mp.reproject(crs, scene.getCRS),
                outputPath = scene.outputPath,
                crs        = scene.getCRS
              )
            }

        Fields(
          name      = scene.name,
          uri       = scene.path,
          list      = fields.toList,
          targetCRS = Some(scene.getCRS)
        )
      }
    }

  def persist(outputPath: String)(implicit cs: ContextShift[IO]): Unit =
    generateFields
      .map(_.map(persistFields(_, outputPath)))
      .parSequence
      .unsafeRunSync

  def send(outputPath: String)(implicit cs: ContextShift[IO]): Unit =
    generateFields
      .map(_.map(sendFields(_, outputPath)))
      .parSequence
      .unsafeRunSync

}


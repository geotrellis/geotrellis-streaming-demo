package com.azavea.generator

import com.azavea._
import com.azavea.streaming._
import com.azavea.conf._
import com.azavea.json._
import io.circe.syntax._
import geotrellis.vector._
import cats.effect._
import cats.implicits._

import scala.util._
import java.io.PrintWriter

import com.azavea.kafka.MessageSender

object Generator {
  lazy val messageSender: MessageSender[String, String] = getMessageSender(IngestStreamConfig.kafka.bootstrapServers)

  def square(size: Int, dx: Double, dy: Double): Line =
    Line(Seq((-size, -size), (size, -size), (size, size), (-size, size), (-size, -size))
      .map { case (x, y) => (x + dx, y + dy) })

  def multiPolygons(width: Int, height: Int, count: Int): Seq[(Int, MultiPolygon)] =
    Try {
      for {
        id <- 1 to count
        size = Random.nextInt(3 * height / 4) + height / 4
        dx = Random.nextInt(width - size) - width / 2 - 0.1
        dy = Random.nextInt(height - size) - height / 2 - 0.1
        border = square(size, dx, dy)
      } yield (id, MultiPolygon(Polygon(border)))
    } match {
      case Success(v) => v
      case Failure(_) => multiPolygons(width, height, count)
    }

  // NOTE: pretty print is done only for demo purposes
  // for a real kafka instance it's recommended to
  def persistFields(fields: Fields, path: String): Unit =
    if(!path.startsWith("s3://") || !path.startsWith("hdfs://")) new PrintWriter(s"$path/${fields.name}.json") { write(fields.asJson.spaces2); close }
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
          multiPolygons(extent.width.toInt, extent.height.toInt, scene.count)
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


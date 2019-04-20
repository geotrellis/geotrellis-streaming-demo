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

package com.azavea

import com.azavea.generator.Generator

import cats.effect.IO
import cats.implicits._
import com.monovore.decline.{CommandApp, Opts}

import scala.concurrent.ExecutionContext

object Main extends CommandApp(
  name = "geotrellis-streaming-producor",
  header = "Test Kafka messages producer",
  main = {
    implicit val cs = IO.contextShift(ExecutionContext.global)

    val outputPath      = Opts.option[String]("outputPath", help = "path to put generated kafka messages").withDefault("../data/json")
    val generateOnly    = Opts.flag("generate-only", help = "Generate only Kafka Messages").orFalse
    val sendGenerated   = Opts.flag("send-generated", help = "Send generated Kafka Messages").orFalse
    val generateAndSend = Opts.flag("generate-and-send", help = "Generate Kafka messages and send them").orTrue
    val skipGeneration  = Opts.flag("skip-persistent", help = "Generate messages and immediately send them into Kafka").orFalse

    (outputPath, generateOnly, sendGenerated, generateAndSend, skipGeneration).mapN { (path, go, seg, gas, sg) =>
      if(go) Generator.persist(path)
      else if (seg) Generator.sendPersisted(path)
      else if (sg) Generator.send(path)
      else { Generator.persist(path); Generator.sendPersisted(path) }
    }
  }
)

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

import com.azavea.kafka.MessageSender
import org.apache.kafka.common.serialization.StringSerializer

package object streaming extends Serializable {
  def getMessageSender(servers: String): MessageSender[String, String] =
    MessageSender[String, String](servers, classOf[StringSerializer].getName, classOf[StringSerializer].getName)
}

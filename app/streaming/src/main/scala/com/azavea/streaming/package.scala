package com.azavea

import com.azavea.kafka.MessageSender
import org.apache.kafka.common.serialization.StringSerializer

package object streaming extends Serializable {
  def getMessageSender(servers: String): MessageSender[String, String] =
    MessageSender[String, String](servers, classOf[StringSerializer].getName, classOf[StringSerializer].getName)
}

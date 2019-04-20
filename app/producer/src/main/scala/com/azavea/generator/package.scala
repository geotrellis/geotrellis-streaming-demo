package com.azavea

import java.io.File

import scala.io.Source

package object generator {
  def getJsonFromFile(file: String): String = {
    val lines = Source.fromFile(file).getLines
    val json = lines.mkString(" ")
    json
  }

  def getListOfFiles(dir: String): List[String] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList.map(_.getAbsolutePath)
    else Nil
  }
}

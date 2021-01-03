package io.github.kirill5k.telegrambot

import scala.io.Source

object TestUtil {
  def readFile(path: String): String = Source.fromResource(path).getLines().toList.mkString
}

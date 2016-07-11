package net.thefallenmoon.mapper.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonHelper {
  val mapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)

  val prettyWriter = mapper.writerWithDefaultPrettyPrinter()
}

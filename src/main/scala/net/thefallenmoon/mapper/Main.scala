package net.thefallenmoon.mapper

import org.rogach.scallop.{ScallopConf, Subcommand}
import better.files._
import java.io.{File => JFile}
import javax.imageio.ImageIO

import net.thefallenmoon.mapper.io.JsonHelper
import net.thefallenmoon.mapper.model.BlockModel

object Main {

  class MapperConf(args: Seq[String]) extends ScallopConf(args) {
    val configFile = trailArg[String](descr = "config file")
    verify()
  }

  def main(args: Array[String]) {
    val conf = new MapperConf(args)

    val configFile = JsonHelper.mapper.readValue(conf.configFile().toFile.contentAsString, classOf[MapConfig])

    val renderer = new DimensionRenderer(configFile)
    renderer.render()
  }
}

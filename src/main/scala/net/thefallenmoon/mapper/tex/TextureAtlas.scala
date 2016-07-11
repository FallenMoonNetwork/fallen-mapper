package net.thefallenmoon.mapper.tex

import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

import better.files.File
import net.thefallenmoon.mapper.tex.AtlasGenerator.TextureFile

import scala.collection.JavaConversions._

object TextureAtlas {
  def buildAtlas(dir: File): TextureAtlas = {
    val textures = dir.glob("**/*.png").map(f => {
      val in = f.newInputStream
      val bi = ImageIO.read(in)
      in.close()

      val name = dir.relativize(f).toString.replace(".png", "").replace('\\', '/')
      new TextureFile(name, bi)
    }).toIndexedSeq

    val pixels = textures.map(t => t.img.getWidth() * t.img.getWidth()).sum
    val largestDimensionX = textures.map(_.img.getWidth).max
    val largestDimensionY = textures.map(_.img.getHeight()).max
    val minSizeX = Math.max(Math.sqrt(pixels).toInt, largestDimensionX)
    val minSizeY = Math.max(Math.sqrt(pixels).toInt, largestDimensionY)
    val powerOfTwoX = Math.ceil(Math.log(minSizeX) / Math.log(2)).toInt
    val powerOfTwoY = Math.ceil(Math.log(minSizeX) / Math.log(2)).toInt
    val sizeX = Math.pow(2, powerOfTwoX).toInt
    var sizeY = Math.pow(2, powerOfTwoY).toInt

    println(s"Total number of texture pixels: $pixels (estimated bounds: $minSizeX x $minSizeY, determined bounds: $sizeX x $sizeY)")

    val texture = try {
      new AtlasGenerator().run(sizeX, sizeY, 0, textures)
    } catch {
      case x: IOException if x.getMessage.toLowerCase.contains("too small") =>
        sizeY =  Math.pow(2, powerOfTwoY + 1).toInt
        println(s"Original size failed to atlas; doubling height ($sizeX x $sizeY).")
        new AtlasGenerator().run(sizeX, sizeY, 0, textures)
      case x: Throwable => throw x
    }

      def mapPixelX(pixel: Int) = pixel.toFloat / sizeX.toFloat
      def mapPixelY(pixel: Int) = pixel.toFloat / sizeY.toFloat

      val mappedTex = texture.getRectangleMap.map(tuple => {
        val (name, rect) = tuple
        name -> Rectangle2f(mapPixelX(rect.x), mapPixelY(rect.y), mapPixelX(rect.width), mapPixelY(rect.height))
      })
      TextureAtlas(mappedTex.toMap, texture.getImage)
  }
}

case class TextureAtlas(itemLocs: Map[String, Rectangle2f], tex: BufferedImage) {

}

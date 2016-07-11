package net.thefallenmoon.mapper

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import com.sun.javafx.iio.ImageStorage.ImageType
import net.thefallenmoon.mapper.io.{JsonHelper, NBTHelper}
import net.thefallenmoon.mapper.mc.{DimensionAccess, RegionFileAccess, WorldInfo}
import net.thefallenmoon.mapper.tex.{Rectangle2f, TextureAtlas}
import net.thefallenmoon.mapper.model.BlockModel

class DimensionRenderer(configFile: MapConfig) {
  println("Loading world info...")
  val worldInfo = WorldInfo.parse(NBTHelper.read(configFile.worldDirectoryFile / "level.dat"))
  println(s"${worldInfo.blockIdMap.size} registered blocks")

  println("Loading models...")
  val models = BlockModel.loadModels(configFile.modelsDirectoryFile)
  println(s"Loaded ${models.size} models")

  println("Loading atlas...")
  val atlas = if ((configFile.outputDirectoryFile / "atlas.json").isRegularFile) {
    println("Loading existing atlas")
    val locsStr = (configFile.outputDirectoryFile / "atlas.json").contentAsString
    val locs = JsonHelper.mapper.readValue(locsStr, classOf[Map[String, Rectangle2f]])
    val tex = ImageIO.read((configFile.outputDirectoryFile / "atlas.png").toJava)
    TextureAtlas(locs, tex)
  } else {
    println("Building atlas...")
    val a = TextureAtlas.buildAtlas(configFile.texturesDirectoryFile)
    ImageIO.write(a.tex, "png", (configFile.outputDirectoryFile / "atlas.png").toJava)
    (configFile.outputDirectoryFile / "atlas.json").write(JsonHelper.mapper.writeValueAsString(a.itemLocs))
    a
  }
  println(s"Loaded ${atlas.itemLocs.size} textures")


  println(s"Finding dimensions...")
  val dims = new DimensionAccess(configFile.worldDirectoryFile)
  println(s"Found ${dims.dims.size} dimensions")
  println(s"Found ${dims.dims.map(_.regionFiles.size).sum} region files total")

  def render(): Unit = {
    dims.dims.foreach(renderDim)
  }

  def renderDim(access: RegionFileAccess): Unit = {
    for (region <- access.regionFiles) {
      val ((x, z), file) = region
      access.clearUneededDims(x, z)
      renderRegion(access.name, x, z, access)
    }
  }

  def renderRegion(dimName: String, x: Int, z: Int, access: RegionFileAccess) = {
    println(s"Rendering $dimName: $x, $z")
    val region = access.getRegion(x, z)
    val outImage = new BufferedImage(32 * 16, 32 * 16, BufferedImage.TYPE_4BYTE_ABGR)

    val xOff = x * 32 * 16
    val zOff = z * 32 * 16

    for (bx <- 0 until 32 * 16) {
      for (bz <- 0 until 32 * 16) {
        var height = access.height(bx + xOff, bz + zOff)
        var block = access.block(bx + xOff, height, bz + zOff)
        val light = access.blockLight(bx + xOff, height, bz + zOff) << 4
        val color = 0xFF000000 | height | light << 8 | block << 16
        outImage.setRGB(bx, bz, color)
      }
    }

    val outfile = configFile.outputDirectoryFile / "images" / dimName / s"r.$x.$z.png"
    outfile.parent.createDirectories()
    ImageIO.write(outImage, "PNG", outfile.toJava)
  }
}

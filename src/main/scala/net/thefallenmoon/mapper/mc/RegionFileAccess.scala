package net.thefallenmoon.mapper.mc

import java.io.{IOException, RandomAccessFile}

import better.files.File
import com.flowpowered.nbt.Tag
import net.thefallenmoon.mapper.io.{ModFixer, NBTHelper}

class RegionFileAccess(val name: String, dimFolder: File) {
  val regionFiles = Map((dimFolder / "region").list.filter(_.name.endsWith("mca")).map(f => {
    val split = f.nameWithoutExtension.split("\\.")

    (split(1).toInt, split(2).toInt) -> f
  }).toSeq: _*)

  private var loadedRegionFiles = Map[(Int, Int), RegionFile]()

  def clearUneededDims(x: Int, z: Int) = {
    loadedRegionFiles = loadedRegionFiles.filter(tuple => {
      val ((rx, rz), _) = tuple
      val xdiff = rx - x
      val zdiff = rz - z

      Math.sqrt(rx * rx + rz * rz) < 2 //Cache in a little area
    })
  }

  def loadRegion(rx: Int, rz: Int, file: File) = {
    val raf = new RandomAccessFile(file.toJava, "r")
    val locs = (0 until 1024).map(_ => raf.readInt())
    val timestamps = (0 until 1024).map(_ => raf.readInt())

    var chunks = Seq[((Int, Int), Tag[_])]()

    for (i <- 0 until 1024) {
      val loc = locs(i)
      val timestamp = timestamps(i)
      val offset = (loc >> 8) * 4096
      val size = (loc & 0xFF) * 4096

      val cx = i % 32
      val cz = i / 32
      if (size > 0) {
        if (size > raf.length()) {
          throw new IOException("Attempted to read past end of region")
        }
        raf.seek(offset)
        val realSize = raf.readInt()
        val compression = raf.readByte()
        if (compression != 2) {
          throw new IllegalArgumentException(s"Unknown comrpession format $compression")
        }
        val tag = NBTHelper.read(raf)
        chunks +:= (cx, cz) -> tag
      }
    }

    new RegionFile(rx, rz, chunks)
  }

  def getRegion(x: Int, z: Int): RegionFile = {
    var res = loadedRegionFiles.get((x, z))
    if (res.isDefined) {
      return res.get
    }
    res = regionFiles.get((x, z))
      .map(loadRegion(x, z, _))
    if (res.isDefined) {
      loadedRegionFiles += (x, z) -> res.get
      return res.get
    }
    new RegionFile(x, z, Seq())
  }

  def block(bx: Int, by: Int, bz: Int) = {
    val (rx: Int, rz: Int) = regionFromBlockCoords(bx, bz)
    getRegion(rx, rz).block(ModFixer.mod(bx, 16 * 32), by, ModFixer.mod(bz, 16 * 32))
  }

  def meta(bx: Int, by: Int, bz: Int) = {
    val (rx: Int, rz: Int) = regionFromBlockCoords(bx, bz)
    getRegion(rx, rz).meta(ModFixer.mod(bx, 16 * 32), by, ModFixer.mod(bz, 16 * 32))
  }

  def blockLight(bx: Int, by: Int, bz: Int) = {
    val (rx: Int, rz: Int) = regionFromBlockCoords(bx, bz)
    getRegion(rx, rz).blockLight(ModFixer.mod(bx, 16 * 32), by, ModFixer.mod(bz, 16 * 32))
  }

  def skyLight(bx: Int, by: Int, bz: Int) = {
    val (rx: Int, rz: Int) = regionFromBlockCoords(bx, bz)
    getRegion(rx, rz).skyLight(ModFixer.mod(bx, 16 * 32), by, ModFixer.mod(bz, 16 * 32))
  }

  def height(bx: Int, bz: Int) = {
    val (rx: Int, rz: Int) = regionFromBlockCoords(bx, bz)
    getRegion(rx, rz).height(ModFixer.mod(bx, 16 * 32), ModFixer.mod(bz, 16 * 32))
  }

  def regionFromBlockCoords(bx: Int, bz: Int): (Int, Int) = {
    var rx = bx / (16 * 32)
    var rz = bz / (16 * 32)
    if (bx < 0) rx -= 1 //Integer math is funky
    if (bz < 0) rz -= 1
    (rx, rz)
  }
}

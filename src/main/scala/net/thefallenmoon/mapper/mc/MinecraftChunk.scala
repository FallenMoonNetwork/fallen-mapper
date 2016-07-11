package net.thefallenmoon.mapper.mc

import com.flowpowered.nbt.{CompoundTag, Tag}
import net.thefallenmoon.mapper.io.NBTHelper._

class MinecraftChunk(val x: Int, val z: Int, nbt: Tag[_]) {
  val sections = Map((nbt / "Level" / "Sections").list[CompoundTag].map(tag => {
    val section = VerticalSection(tag)
    (section.y, section)
  }):_*)

  val heightMap = (nbt / "Level" / "HeightMap").intArray.get

  def block(bx: Int, by: Int, bz: Int) = {
    val section = by / 16
    sections.get(section).map(_.block(bx, by % 16, bz)).getOrElse(0)
  }

  def meta(bx: Int, by: Int, bz: Int) = {
    val section = by / 16
    sections.get(section).map(_.meta(bx, by % 16, bz)).getOrElse(0)
  }

  def blockLight(bx: Int, by: Int, bz: Int) = {
    val section = by / 16
    sections.get(section).map(_.blockLight(bx, by % 16, bz)).getOrElse(0)
  }

  def skyLight(bx: Int, by: Int, bz: Int) = {
    val section = by / 16
    sections.get(section).map(_.skyLight(bx, by % 16, bz)).getOrElse(15)
  }

  def height(bx: Int, bz: Int) = {
    heightMap(bz * 16 + bx)
  }
}

case class VerticalSection(tag: CompoundTag) {
  val y = (tag / "Y").int.get
  val add = if (tag.getValue.containsKey("Add")) {
    (tag / "Add").byteArray.get
  } else {
    new Array[Byte](2048)
  }
  val blocks = (tag / "Blocks").byteArray.get
  val meta = (tag / "Data").byteArray.get
  val blockLight = (tag / "BlockLight").byteArray.get
  val skyLight = (tag / "SkyLight").byteArray.get

  def block(bx: Int, by: Int, bz: Int): Int = {
    val idx = index(bx, by, bz)
    blocks(idx) + nibble(add, idx)
  }

  def meta(bx: Int, by: Int, bz: Int): Int = {
    val idx = index(bx, by, bz)
    nibble(meta, idx)
  }

  def blockLight(bx: Int, by: Int, bz: Int): Int = {
    val idx = index(bx, by, bz)
    nibble(blockLight, idx)
  }

  def skyLight(bx: Int, by: Int, bz: Int): Int = {
    val idx = index(bx, by, bz)
    nibble(skyLight, idx)
  }

  def index(x: Int, z: Int, y: Int) = (z * 16 + y) * 16 + x

  def nibble(arr: Array[Byte], idx: Int): Int = {
    val realIdx = idx / 2
    if (idx % 2 == 1) {
      ((arr(realIdx) >> 4) & 0xF).toByte
    } else {
      (arr(realIdx) & 0xF).toByte
    }
  }
}

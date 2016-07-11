package net.thefallenmoon.mapper.mc

import com.flowpowered.nbt.Tag
import net.thefallenmoon.mapper.io.ModFixer

class RegionFile(val x: Int, val z: Int, nbt: Seq[((Int, Int), Tag[_])]) {
  val chunks = Map(nbt.map(tuple => {
    val ((x, z), tag) = tuple
    (x, z) -> new MinecraftChunk(x, z, tag)
  }): _*)

  def block(bx: Int, by: Int, bz: Int) = {
    val cx = bx / 16
    val cz = bz / 16
    chunks.get((cx, cz)).map(_.block(bx % 16, by, bz % 16)).getOrElse(0)
  }

  def meta(bx: Int, by: Int, bz: Int) = {
    val cx = bx / 16
    val cz = bz / 16
    chunks.get((cx, cz)).map(_.meta(bx % 16, by, bz % 16)).getOrElse(0)
  }

  def blockLight(bx: Int, by: Int, bz: Int) = {
    val cx = bx / 16
    val cz = bz / 16
    chunks.get((cx, cz)).map(_.blockLight(bx % 16, by, bz % 16)).getOrElse(0)
  }

  def skyLight(bx: Int, by: Int, bz: Int) = {
    val cx = bx / 16
    val cz = bz / 16
    chunks.get((cx, cz)).map(_.skyLight(bx % 16, by, bz % 16)).getOrElse(0)
  }

  def height(bx: Int, bz: Int) = {
    val cx = bx / 16
    val cz = bz / 16
    chunks.get((cx, cz)).map(_.height(bx % 16, bz % 16)).getOrElse(0)
  }
}

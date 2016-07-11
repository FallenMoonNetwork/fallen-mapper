package net.thefallenmoon.mapper.io

import java.io.RandomAccessFile
import java.util
import java.util.zip._

import better.files._
import com.flowpowered.nbt._
import com.flowpowered.nbt.stream.NBTInputStream

import scala.reflect._

object NBTHelper {
  def read(file: File): Tag[_] = {
    val in = file.newInputStream
    val nbtIn = new NBTInputStream(in)
    val res = nbtIn.readTag()
    in.close()
    res
  }

  def read(file: RandomAccessFile): Tag[_] = {
    val in = new RandomAccessFileInputStream(file)
    val zipIn = new InflaterInputStream(in)
    val nbtIn = new NBTInputStream(zipIn, false)
    val res = nbtIn.readTag()
    res
  }

  implicit class RichTag[T](tag: Tag[T]) {
    def compound: CompoundTag = tag match {
      case ct: CompoundTag => ct
    }

    def list[Q <: Tag[_] : ClassTag]: ListTag[Q] = tag match {
      case lt: ListTag[Q] => lt
      case _ => new ListTag[Q]("", null, new util.ArrayList[Q]())
    }

    def string: Option[String] = tag match {
      case st: StringTag => Some(st.getValue)
      case _ => None
    }

    def int: Option[Int] = tag match {
      case it: IntTag => Some(it.getValue)
      case st: ShortTag => Some(st.getValue.toInt)
      case bt: ByteTag => Some(bt.getValue.toInt)
      case _ => None
    }

    def byteArray = tag match {
      case t: ByteArrayTag => Some(t.getValue)
      case _ => None
    }

    def shortArray = tag match {
      case t: ShortArrayTag => Some(t.getValue)
      case _ => None
    }

    def intArray = tag match {
      case t: IntArrayTag => Some(t.getValue)
      case _ => None
    }

    def /(name: String) = compound.getValue.get(name) match {
      case null => new CompoundTag("", new CompoundMap())
      case x => x
    }

    def apply[Q](index: Int) = list.getValue.get(index)
  }

  implicit class RichListTag[T <: Tag[_]](tag: ListTag[T]) extends IndexedSeq[T] {
    val value = tag.getValue

    override def length: Int = value.size()
    override def apply(idx: Int): T = value.get(idx)
  }

}

package net.thefallenmoon.mapper.io

import java.io.FileNotFoundException

import better.files._

object ResourceUtils {
  def read(path: String): String = {
    val in = getClass.getClassLoader.getResourceAsStream(path)
    if (in == null) {
      throw new FileNotFoundException(path)
    }
    val str = new String(in.bytes.toArray)
    in.close()

    str
  }
}

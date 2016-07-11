package net.thefallenmoon.mapper.io

object ModFixer {
  def mod(a: Int, b: Int) = {
    val res = a % b
    if (res < 0) {
      res + b
    } else {
      res
    }
  }
}

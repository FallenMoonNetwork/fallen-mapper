package net.thefallenmoon.mapper.mc

import better.files.File

class DimensionAccess(worldDirectoryFile: File) {
  val dimDirs = (worldDirectoryFile.list.filter(_.isDirectory).filter(f => (f / "region").exists) ++ Seq(worldDirectoryFile)).toIndexedSeq
  val dims = dimDirs.map(f => new RegionFileAccess(f.name, f))
}

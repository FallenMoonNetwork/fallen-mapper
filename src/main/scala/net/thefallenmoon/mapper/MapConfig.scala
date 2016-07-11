package net.thefallenmoon.mapper
import better.files._
import java.io.{File => JFile}
import java.nio.file.Files

case class MapConfig(worldDirectory: String,
                     modelsDirectory: String,
                     texturesDirectory: String,
                     outputDirectory: String) {

  def validate(): Unit = {
    if (!worldDirectoryFile.isDirectory) {
      throw new IllegalArgumentException("World direcotry is not a directory or does not exist")
    }
    if (!modelsDirectoryFile.isDirectory) {
      throw new IllegalArgumentException("World direcotry is not a directory or does not exist")
    }
    if (!texturesDirectoryFile.isDirectory) {
      throw new IllegalArgumentException("World direcotry is not a directory or does not exist")
    }
    if (!(outputDirectoryFile.isDirectory || !outputDirectoryFile.exists)) {
      throw new IllegalArgumentException("World direcotry exists and is not a directory")
    }
  }

  val worldDirectoryFile = worldDirectory.toFile
  val modelsDirectoryFile = modelsDirectory.toFile
  val texturesDirectoryFile = texturesDirectory.toFile
  val outputDirectoryFile = outputDirectory.toFile
  validate()
  outputDirectoryFile.createDirectories()
}

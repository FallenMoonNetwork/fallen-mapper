package net.thefallenmoon.mapper.mc

import com.flowpowered.nbt.{CompoundTag, Tag}
import net.thefallenmoon.mapper.io.{JsonHelper, ResourceUtils}
import net.thefallenmoon.mapper.io.NBTHelper._
import better.files._

object WorldInfo {
  def parse(value: Tag[_]) = {
    val blockIds = (value / "FML" / "Registries" / "minecraft:blocks" / "ids")
      .list[CompoundTag]
      .map(v => (v / "K").string.get -> (v / "V").int.get)

    val blockIdMap = if (blockIds.nonEmpty) {
      Map(blockIds: _*)
    } else {
      println("No FML block list present; defaulting to default minecraft 1.10 list")
      val str = ResourceUtils.read("default-block-list.json")
      JsonHelper.mapper.readValue(str, classOf[Map[String, Int]])
    }

    val version = (value / "Data" / "Version" / "Name").string.getOrElse("Unknown")

    WorldInfo(
      version = version,
      blockIdMap = blockIdMap
    )
  }

}

case class WorldInfo(version: String,
                     blockIdMap: Map[String, Int]) {
  "./out/default-block-list.json".toFile.write(JsonHelper.mapper.writeValueAsString(blockIdMap))
}

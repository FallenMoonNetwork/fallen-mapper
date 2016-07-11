package net.thefallenmoon.mapper.model

import better.files._
import net.thefallenmoon.mapper.io.JsonHelper

object BlockModel {
  def loadModels(path: File): Map[String, BlockModel] = {
    val models = path.glob("**/*.json").map(model => JsonHelper.mapper.readValue(model.contentAsString, classOf[BlockModel])).toSeq

    Map(models.map(m => (m.name + "-" + m.meta) -> m):_*)
  }

}

case class BlockModel(name: String,
                      stateName: String,
                      opaque: Boolean,
                      meta: Int,
                      quads: Map[EnumFacing, Seq[DumpedQuad]]) {
}
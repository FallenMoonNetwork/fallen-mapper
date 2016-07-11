package net.thefallenmoon.mapper.model

import java.util.ArrayList
import java.util.List

case class DumpedQuad(texture: String,
                      verts: Seq[Vector3f],
                      uv: Seq[Vector2f],
                      normals: Seq[Vector3f],
                      colors: Seq[Color4f]) {
}
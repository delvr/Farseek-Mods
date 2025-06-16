package streams.world.gen

import farseek.game.*
import farseek.game.imports.*
import farseek.util.*
import streams.world.gen.segments.*

final case class StreamsChunk(segment: Segment, columns: Map[BlockXZ, StreamsColumn]):
  def preCarve(chunk: ProtoChunk): Unit =
    given BlockYBox = chunk.yBounds
    if segment.surfaceLevel > segment.generator.minSurfaceLevel then
      segment.generator.addFloor(chunk, segment.surfaceLevel, segment.maxDepth)
    columns.values.foreach(_.preCarve(chunk))

  def build()(using WorldGenLevel, BlockYBox): Unit = columns.values.foreach(_.build())

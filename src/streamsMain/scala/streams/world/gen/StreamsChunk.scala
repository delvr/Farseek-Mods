package streams.world.gen

import farseek.game.{*, given}
import farseek.game.imports.*
import farseek.util.{*, given}
import net.neoforged.neoforge.event.level.ChunkEvent
import streams.world.gen.segments.*

final case class StreamsChunk(segment: Segment, columns: Map[BlockXZ, StreamsColumn]):
  def preCarve(chunk: ProtoChunk): Unit =
    if segment.surfaceLevel > segment.generator.minSurfaceLevel then
      addFloor(chunk, segment.generator.baseRock, segment.surfaceLevel, segment.maxDepth)
    columns.values.foreach(_.preCarve(chunk))

  def build()(using WorldGenLevel, BlockYBox): Unit = columns.values.foreach(_.build())
end StreamsChunk

// Event handlers
// ------------------------------------------------------------------------------------------------
def preBuildSurface(event: PreBuildSurface): Unit =
  import event.*
  given ChunkGenerator = generator
  given NoiseState = noise
  StreamsGenerator.getOrCreate.foreach(generator => addFloor(
    chunk, generator.baseRock, generator.minSurfaceLevel, generator.maxRiverDepth))
  chunk.pxz.chunkData.foreach(_.preCarve(chunk))

def postBuildSurface(event: PostBuildSurface): Unit =
  import event.*
  given ChunkGenerator = generator
  given NoiseState = noise
  given WorldGenRegion = region
  chunk.pxz.chunkData.foreach(_.build())

def chunkLoaded(event: ChunkEvent.Load): Unit =
  import event.*
  getLevel match
    case level: ServerLevel =>
      given ChunkGenerator = level.chunkGenerator
      StreamsGenerator.getExisting.foreach: generator =>
        val p = getChunk.pxz
        // don't invalidate in absent segments when reloading existing chunks
        generator.existingSegmentAt(p).foreach(_.invalidate(p))
    case _ =>

def addFloor(chunk: Chunk, state: BlockState, surfaceLevel: BlockY, thickness: PositiveInt): Unit =
  given Chunk = chunk
  for
    xz <- chunk.into[BlockXZBox].elements
    floorNoise = if xz.at(surfaceLevel).isEmptyBlock then FloorNoises.first.zeroOrOneAt(xz) else 0
    p  <- xz.at(surfaceLevel :+ floorNoise).downwards.take(thickness + 1 + CeilingNoise.zeroOrOneAt(xz))
    if p.isEmptyBlock
  do p.setBlock(state)

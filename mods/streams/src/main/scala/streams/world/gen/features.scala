package streams.world.gen

import farseek.util.*
import farseek.game.imports.*
import farseek.game.{*, given}
import net.minecraft.world.level.*
import net.minecraft.world.level.levelgen.feature.Feature.*
import streams.*

extension(p: into BlockXYZ)
  def isValidTreePos(using reader: LevelSimulatedReader): Boolean = reader match
    case level: ServerLevel =>
      given ChunkGenerator = level.chunkGenerator
      given RandomState = level.randomState
      !columnAt(p).exists(col => col.isStreamBed && col.isFallBase)
    case _                  => true

private lazy val AllowedFeatures = Set[Feature[?]](DISK, SEAGRASS, FREEZE_TOP_LAYER)

def filterFeature(event: PlaceFeature[?, ?]): Unit =
  import event.*
  given ChunkGenerator = generator
  given RandomState = level.randomState
  val p = here.xyz
  if !AllowedFeatures.contains(feature) && columnAt(p).exists(col =>
      col.maxFloorLevel < col.segment.surfaceLevel && col.minClearLevel >= p.y) then
    event.cancel()

def filterStructure(event: GenerateStructure): Unit =
  import event.*
  given ChunkGenerator = context.chunkGenerator
  given RandomState = context.randomState
  pieces.filterInPlace: piece =>
    !piece.getBoundingBox.into[BlockXYZBox].xz.corners.exists(columnAt(_).isDefined)

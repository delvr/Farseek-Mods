package streams.world.gen

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.world.level.*
import net.minecraft.world.level.levelgen.feature.Feature.*
import streams.*

extension(p: into[BlockXYZ])
  def isValidTreePos(using reader: LevelSimulatedReader): Boolean = reader match
    case level: ServerLevel =>
      given ChunkGenerator = level.chunkGenerator
      given NoiseState = level.noiseState
      !columnAt(p).exists(col => col.isStreamBed && col.isFallBase)
    case _                  => true

private lazy val AllowedFeatures = Set[Feature[?]](DISK, SEAGRASS, FREEZE_TOP_LAYER)

def filterFeature(event: PlaceFeature[?, ?]): Unit =
  import event.*
  given ChunkGenerator = generator
  given NoiseState = level.noiseState
  val p = here.blockXYZ
  if !AllowedFeatures(feature) && columnAt(p).exists(col =>
      col.maxFloorLevel < col.segment.surfaceLevel && col.minClearLevel >= p.y) then
    event.cancel()

def filterStructure(event: GenerateStructure): Unit =
  import event.*
  given ChunkGenerator = context.chunkGenerator
  given NoiseState = context.randomState
  pieces.filterInPlace: piece =>
    !piece.getBoundingBox.into[BlockXYZBox].xz.corners.exists(columnAt(_).isDefined)

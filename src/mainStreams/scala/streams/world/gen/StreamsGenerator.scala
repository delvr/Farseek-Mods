package streams.world.gen

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.*
import net.minecraft.world.level.levelgen.*
import streams.world.gen.segments.*

// Accessors
// ------------------------------------------------------------------------------------------------
extension(p: into[BasinXZ])(using ChunkGenerator, NoiseState)
  def segment: Option[BasinSegment] = StreamsGenerator.getOrCreate.flatMap(_.getOrCreateSegmentAt(p))

extension(p: into[ChunkXZ])(using ChunkGenerator, NoiseState)
  def chunkData: Option[StreamsChunk] = p.segment.flatMap(_.chunkData.option(p))

extension(p: into[BlockXZ])(using ChunkGenerator, NoiseState)
  def columnAt: Option[StreamsColumn] = p.chunkData.flatMap(_.columns.get(p))

object StreamsGenerator:
  // Generators
  // ------------------------------------------------------------------------------------------------
  private val generators = MutableMap[NoiseBasedChunkGenerator, Option[StreamsGenerator]]()
  def getExisting(using chunkGenerator: ChunkGenerator): Option[StreamsGenerator] = chunkGenerator match
    case gen: NoiseBasedChunkGenerator => generators.get(gen).flatten
    case _ => None
  def getOrCreate(using chunkGenerator: ChunkGenerator): Option[StreamsGenerator] = chunkGenerator match
    case gen: NoiseBasedChunkGenerator => generators.getOrElseUpdate(gen, Try(StreamsGenerator(using gen)).toOption)
    case _ => None

end StreamsGenerator

final class StreamsGenerator private(using chunkGenerator: NoiseBasedChunkGenerator):
  // Level settings
  // -----------------------------------------------------------------------------------------------
  private val genSettings: NoiseGeneratorSettings = chunkGenerator.settings
  import genSettings.*
  // https://gist.github.com/jacobsjo/0ce1f9d02e5c3e490e228ac5ad810482
  private val quickGenSettings = NoiseGeneratorSettings(noiseSettings, defaultBlock, defaultFluid,
    noiseRouter, surfaceRule, spawnTarget, seaLevel, disableMobGeneration: @nowarn,
    aquifersEnabled = false, oreVeinsEnabled = false, useLegacyRandomSource)
  private given levelBounds: BlockYBox = summon
  val baseRock: BlockState = defaultBlock
  val seaFluid: BlockState = defaultFluid
  val hasLava = seaFluid.hasLava

  // Elevations and Y-bounds
  // -----------------------------------------------------------------------------------------------
  val maxRiverDepth     = if hasLava then `2` else `8`
  val maxTributaryDepth = if hasLava then `2` else `4`
  val maxTunnelHeight          = `16`
  val maxTributaryTunnelHeight =  `8`
  val minSurfaceLevel = BlockY(seaLevel) :- 1
  val maxSurfaceLevel = levelBounds.max :- maxTributaryTunnelHeight :- 1
  private val surfaceBounds  = minSurfaceLevel <-> maxSurfaceLevel
  private val minBottomLevel = minSurfaceLevel :-  maxRiverDepth
  require(levelBounds contains minBottomLevel)

  // Terrain noise
  // -----------------------------------------------------------------------------------------------
  private val noiseAreas = MapWithInvalidation[BasinXZ, NoiseArea]()

  private def newNoiseAreaAt(p: ChunkXZ)(using NoiseState): NoiseArea =
    given NoiseGeneratorSettings = quickGenSettings
    chunkGenerator.noiseArea(p.min, BasinChunkSize ** ChunkWidth)

  private def noiseAreaAt(p: into[BasinXZ])(using NoiseState): NoiseArea =
    noiseAreas.getOrElseUpdate(p, newNoiseAreaAt(p.min))

//  private def invalidateNoiseAreaAt(p: into[BasinXZ]) = noiseAreas.invalidate(p)

  private def leaksOut(p: into[BlockXZ])(using NoiseState): Boolean =
    likelyInSea(p.at(if hasLava then minSurfaceLevel else minBottomLevel.above))

  def likelyInSea(p: BlockXYZ)(using noise: NoiseState): Boolean =
    p.y <= minSurfaceLevel &&
      // pre-filter for Overworld; can be a false positive for a sealed flooded cave near surface
      approximateGroundLevelAt(p).forall(_ < p.y) &&
      // real check, more expensive as it needs to build full column
      chunkGenerator.noiseColumnAt(p).apply(p.y).isWet

  def maxSurfaceAt(p: BlockXZ)(using NoiseState): BlockY =
    approximateGroundLevelAt(p).getOrElse(lowestSurfaceAt(p)).clampedIn(surfaceBounds)

  // https://github.com/TheForsakenFurby/Surface-Rules-Guide-Minecraft-JE-1.18/blob/main/Guide.md#condition--above_preliminary_surface
  // Update: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9
  private def approximateGroundLevelAt(p: into[BlockXZ])(using noise: NoiseState): Option[BlockY] =
    noiseAreaAt(p.into[ChunkXZ]).preliminarySurfaceLevel(p.x.toInt, p.z.toInt)
      .someWhen(_ != 0)    // Nether, End etc. use a dummy Constant(0.0) density function
      .map(BlockY(_) :+ 8) // same adjustment as Minecraft's own usage of preliminarySurfaceLevel

  private def lowestSurfaceAt(p: into[BlockXZ])(using NoiseState): BlockY =
    val column = chunkGenerator.noiseColumnAt(p)
    surfaceBounds.upwards.find(column(_).isAir).map(_.below).getOrElse(surfaceBounds.max)

  // Basin segments
  // -----------------------------------------------------------------------------------------------
  private val basinSegments = MutableMap[BasinXZ, Option[BasinSegment]]()

  def existingSegmentAt(p: into[BasinXZ]): Option[BasinSegment] = basinSegments.get(p).flatten

  def getOrCreateSegmentAt(p: into[BasinXZ])(using NoiseState): Option[BasinSegment] =
    basinSegments.getOrElseUpdate(p, newSegment(p))

  private def newSegment(p: BasinXZ)(using NoiseState): Option[BasinSegment] =
    if p.isOnMainRow then
      if (p :+ East).isOrWillBeUpstreamSegment then p.mouthOutlets.nonEmptyOption match
        case Some(outlets) => Some(Mouth(this, p, outlets.sortBy(_.length).take(3).assumedNonEmpty))
        case None          => Some(Reach(this, p))
      else if p.mouthOutlets.isEmpty then Some(TributaryBasin(this, p, West)) // river source
      else None
    else if p.isSouthTributaryCandidate then Some(TributaryBasin(this, p, North))
    else if p.isNorthTributaryCandidate then Some(TributaryBasin(this, p, South))
    else None

  extension(p: BasinXZ)
    def bounds: BlockXZBox = p.into[ChunkXZBox].converted
    def isOnMainRow : Boolean = p.z.isMultipleOf(MainBasinZSpacing)

  extension(p: BasinXZ)(using NoiseState)
    // don't auto-create on segment lookup, or will recurse forever
    private def isOrWillBeUpstreamSegment: Boolean = basinSegments.get(p) match
      case Some(Some(segment)) => segment.isInstanceOf[UpstreamSegment]
      case Some(None) => false
      case None => mouthOutlets.isEmpty
    private def isLandlocked: Boolean = !p.bounds.facets.values.flatten.exists(leaksOut)
    private def mouthOutlets: ISeq[NonEmptySeq[BlockXZ]] =
      val b = p.bounds
      (b.westwards.map((_, b.maxZ)) ++
       b.northwards.tail.map((b.minX, _)) ++
       b.eastwards.tail.map((_, b.minZ))).slicesWhere(leaksOut).filter(_.length >= 3)
    private def isTributaryCandidate(downstream: XZSide,
        downstreamRequirement: BasinXZ => Boolean): Boolean =
      val downstreamPos = p :+ downstream
      downstreamRequirement(downstreamPos) && downstreamPos.isOnMainRow &&
        getOrCreateSegmentAt(downstreamPos).exists(_.isInstanceOf[Reach]) && p.isLandlocked
    private def isSouthTributaryCandidate: Boolean =
      isTributaryCandidate(downstream = North, downstreamRequirement = _.bendsLeft)
    private def isNorthTributaryCandidate: Boolean =
      isTributaryCandidate(downstream = South, downstreamRequirement = _.bendsRight)

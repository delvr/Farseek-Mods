package streams.world.gen

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.*
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.blending.*
import net.minecraft.world.level.levelgen.DensityFunctions.*
import net.neoforged.neoforge.event.level.*
import streams.world.gen.segments.*

// Accessors
// ------------------------------------------------------------------------------------------------
extension(p: into BasinXZ)(using ChunkGenerator, RandomState)
  def segment: Option[BasinSegment] = StreamsGenerator.getOrCreate.flatMap(_.getOrCreateSegmentAt(p))

extension(p: into ChunkXZ)(using ChunkGenerator, RandomState)
  def chunkData: Option[StreamsChunk] = p.segment.flatMap(_.chunkData.option(p))

extension(p: into BlockXZ)(using ChunkGenerator, RandomState)
  def columnAt: Option[StreamsColumn] = p.chunkData.flatMap(_.columns.get(p))

object StreamsGenerator:
  // Generators
  // ------------------------------------------------------------------------------------------------
  private val generators = MutableMap[NoiseBasedChunkGenerator, Option[StreamsGenerator]]()
  private def getExisting(using chunkGenerator: ChunkGenerator): Option[StreamsGenerator] = chunkGenerator match
    case gen: NoiseBasedChunkGenerator => generators.get(gen).flatten
    case _ => None
  def getOrCreate(using chunkGenerator: ChunkGenerator): Option[StreamsGenerator] = chunkGenerator match
    case gen: NoiseBasedChunkGenerator => generators.getOrElseUpdate(gen, Try(StreamsGenerator(gen)).toOption)
    case _ => None

  // Event handlers
  // ------------------------------------------------------------------------------------------------
  def preBuildSurface(event: PreBuildSurface): Unit =
    import event.*
    given ChunkGenerator = generator
    given RandomState = randomState
    StreamsGenerator.getOrCreate.foreach(_.addFloor(chunk))
    chunk.pxz.chunkData.foreach(_.preCarve(chunk))

  def postBuildSurface(event: PostBuildSurface): Unit =
    import event.*
    given ChunkGenerator = generator
    given RandomState = randomState
    given WorldGenRegion = region
    chunk.pxz.chunkData.foreach(_.build())

  def chunkLoaded(event: ChunkEvent.Load): Unit =
    import event.*
    getLevel match
      case level: ServerLevel =>
        given ChunkGenerator = level.chunkGenerator
        StreamsGenerator.getExisting.foreach: generator =>
          val p = getChunk.pxz
          // don't invalidate in absent segments when reloading existing chunks!
          generator.existingSegmentAt(p).foreach: segment =>
            generator.invalidateNoiseAreaAt(p)
            segment.invalidate(p)
      case _ =>

end StreamsGenerator

final case class StreamsGenerator private(chunkGenerator: NoiseBasedChunkGenerator):
  // Level settings
  // -----------------------------------------------------------------------------------------------
  private val settings = noiseGeneratorSettings
  private val noiseSettings = settings.noiseSettings
  private val levelHeight = LevelHeight(noiseSettings.minY, PositiveInt(noiseSettings.height))
  private val levelBounds = levelHeight.yBounds
  private val fluidPicker = chunkGenerator.globalFluidPicker.get
  val baseRock: BlockState = settings.defaultBlock
  val seaFluid: BlockState = settings.defaultFluid
  // https://gist.github.com/jacobsjo/0ce1f9d02e5c3e490e228ac5ad810482
  private def noiseGeneratorSettings =
    val fullSettings = chunkGenerator.generatorSettings.value
    import fullSettings.*
    NoiseGeneratorSettings(fullSettings.noiseSettings, defaultBlock, defaultFluid, noiseRouter, surfaceRule, spawnTarget,
      seaLevel, disableMobGeneration: @nowarn, aquifersEnabled = false, oreVeinsEnabled = false, useLegacyRandomSource)

  // Elevations and Y-bounds
  // -----------------------------------------------------------------------------------------------
  val maxRiverDepth    : PositiveInt = if seaFluid.hasLava then `2` else `8`
  val maxTributaryDepth: PositiveInt = if seaFluid.hasLava then `2` else `4`
  val maxTunnelHeight         : PositiveInt = `16`
  val maxTributaryTunnelHeight: PositiveInt =  `8`
  val minSurfaceLevel: BlockY = BlockY(settings.seaLevel) :- 1
  val maxSurfaceLevel: BlockY = levelBounds.max :- maxTributaryTunnelHeight :- 1
  private val surfaceBounds  = minSurfaceLevel <-> maxSurfaceLevel
  private val minBottomLevel = minSurfaceLevel :-  maxRiverDepth
  require(levelBounds contains minBottomLevel)

  // Terrain noise
  // -----------------------------------------------------------------------------------------------
  private val noiseSize = ChunkWidth / noiseSettings.getCellWidth
  private val noiseAreas = MapWithInvalidation[ChunkXZ, NoiseArea]()

  private def newNoiseAreaAt(p: into ChunkXZ)(using randomState: RandomState): NoiseArea =
    p.minBlockXZ.usingXZ(new NoiseArea(noiseSize, randomState, _, _, noiseSettings,
      BeardifierMarker.INSTANCE, settings, fluidPicker, Blender.empty))

  private def invalidateNoiseAreaAt(p: into ChunkXZ) = noiseAreas.invalidate(p)

  def likelyInSea(p: BlockXYZ)(using randomState: RandomState): Boolean =
    p.y <= minSurfaceLevel && approximateGroundLevelAt(p).map(_ <= p.y).getOrElse(
      p.usingXZ((x, z) => chunkGenerator.getBaseColumn(x, z, levelHeight, randomState)
        .getBlock(BlockY.toInt(p.y)).isWet))

  def maxSurfaceAt(p: BlockXZ)(using RandomState): BlockY =
    surfaceBounds.clamp(approximateGroundLevelAt(p).orElse(highestFloorAt(p))
      .getOrElse(surfaceBounds.max))

  // https://github.com/TheForsakenFurby/Surface-Rules-Guide-Minecraft-JE-1.18/blob/main/Guide.md
  // https://bugs.mojang.com/browse/MCPE/issues/MCPE-172251
  private def approximateGroundLevelAt(p: into BlockXZ)(using RandomState): Option[BlockY] =
    p.usingXZ: (x, z) =>
      BlockY(noiseAreas.getOrElseUpdate(p, newNoiseAreaAt(p)).preliminarySurfaceLevel(x, z) + 8)
        .someWhenIn(levelBounds)

  private def highestFloorAt(p: into BlockXZ)(using randomState: RandomState): Option[BlockY] =
    p.usingXZ: (x, z) =>
      val noiseColumn = chunkGenerator.getBaseColumn(x, z, levelHeight, randomState)
      def blockAt(y: BlockY) = noiseColumn.getBlock(BlockY.toInt(y))
      levelBounds.downwards.dropWhile(blockAt(_).isSolidBlock).find(blockAt(_).isSolidBlock)

  extension(p: into BlockXZ)(using RandomState)
    private def leaksOut: Boolean = likelyInSea(p.at(if seaFluid.hasLava then minSurfaceLevel else minBottomLevel))

  // Basin segments
  // -----------------------------------------------------------------------------------------------
  private val basinSegments = MutableMap[BasinXZ, Option[BasinSegment]]()

  private def existingSegmentAt(p: into BasinXZ): Option[BasinSegment] = basinSegments.get(p).flatten

  def getOrCreateSegmentAt(p: into BasinXZ)(using RandomState): Option[BasinSegment] =
    basinSegments.getOrElseUpdate(p, newSegment(p))

  private def newSegment(p: BasinXZ)(using RandomState): Option[BasinSegment] =
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
    private def bounds: BlockXZBox = p.into[ChunkXZBox].into[BlockXZBox]
    private def isOnMainRow : Boolean = p.z.isMultipleOf(MainBasinZSpacing)
    // don't auto-create on segment lookup, or will recurse forever
    private def isOrWillBeUpstreamSegment(using RandomState): Boolean = basinSegments.get(p) match
      case Some(Some(segment)) => segment.isInstanceOf[UpstreamSegment]
      case Some(None) => false
      case None => mouthOutlets.isEmpty
    private def isLandlocked(using RandomState): Boolean =
      !bounds.facets.values.flatten.exists(_.leaksOut)
    private def mouthOutlets(using RandomState): ISeq[NonEmptySeq[BlockXZ]] =
      val b = bounds
      (b.westwards.map((_, b.maxZ)) ++
       b.northwards.tail.map((b.minX, _)) ++
       b.eastwards.tail.map((_, b.minZ))).slicesWhere(_.leaksOut).filter(_.length >= 3)
    private def isTributaryCandidate(downstream: XZSide, downstreamRequirement: BasinXZ => Boolean)(using RandomState): Boolean =
      val downstreamPos = p :+ downstream
      downstreamRequirement(downstreamPos) && downstreamPos.isOnMainRow &&
        getOrCreateSegmentAt(downstreamPos).exists(_.isInstanceOf[Reach]) && p.isLandlocked
    private def isSouthTributaryCandidate(using RandomState): Boolean =
      isTributaryCandidate(downstream = North, downstreamRequirement = _.bendsLeft)
    private def isNorthTributaryCandidate(using RandomState): Boolean =
      isTributaryCandidate(downstream = South, downstreamRequirement = _.bendsRight)

  // Chunk modifications
  // -----------------------------------------------------------------------------------------------
  def addFloor(chunk: Chunk, surfaceLevel: BlockY = minSurfaceLevel, thickness: PositiveInt = maxRiverDepth): Unit =
    given Chunk = chunk
    for
      xz <- chunk.into[BlockXZBox].elements
      floorNoise = if xz.at(surfaceLevel).isEmptyBlock then FloorNoises.first.zeroOrOneAt(xz) else 0
      p  <- xz.at(surfaceLevel :+ floorNoise).downwards.take(thickness + 1 + CeilingNoise.zeroOrOneAt(xz))
      if p.isEmptyBlock
    do p.setBlock(baseRock)

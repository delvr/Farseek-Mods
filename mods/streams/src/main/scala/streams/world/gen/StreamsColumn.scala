package streams.world.gen

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.{*, given}
import net.minecraft.world.level.block.Blocks.*
import net.neoforged.neoforge.common.Tags.Biomes.*
import streams.blocks.*
import streams.world.gen.segments.*

private val FloorNoises = zeroTo(`8`).map(n => NoiseGenerator(
  scale = PositiveReal(if n == 0 then 15 else 10 + n), seed = n))
private val CeilingNoise = NoiseGenerator(scale = `10`.toReal, seed = 0)

final case class StreamsColumn(segment: Segment, pos: BlockXZ, maxFloorLevel: BlockY, minClearLevel: BlockY,
    airFlow: Option[BlockState] = None, isFallRim: Boolean = false, isFallBase: Boolean = false):

  def isWall: Boolean = maxFloorLevel >= minClearLevel :- 1 //disallow 1-height gaps (would be filled by ceiling hardening)
  /** [[https://en.wikipedia.org/wiki/Stream_bed stream bed]] */
  def isStreamBed: Boolean = maxFloorLevel < segment.surfaceLevel
  /** [[https://en.wikipedia.org/wiki/Bank_(geography) stream bank]] */
  def isStreamBank: Boolean = maxFloorLevel >= segment.surfaceLevel

  private lazy val surfacePos  = pos.at(segment.surfaceLevel)
  private lazy val minFloorPos: BlockXYZ = pos.at(segment.surfaceLevel :- segment.maxDepth)

  def preCarve(chunk: ProtoChunk)(using BlockYBox): Unit =
    given ProtoChunk = chunk
    for p <- minFloorPos.upwardsTo(segment.surfaceLevel) do
      preventCarvingAt(p)
    if !isWall then
      // go upwards to minimize height-map updates
      val floorLevel = actualFloorLevel
      for p <- pos.at(floorLevel).upwardsTo(clearLevelWithNoise(minClearLevel)) do
        if p.y <= segment.surfaceLevel then
          preventCarvingAt(p)
          if p.y > floorLevel && p.isDry then
            p.setBlock(segment.generator.seaFluid)
        else if p.y > floorLevel && p.nonEmptyBlock then
          p.removeBlock()

  private def preventCarvingAt(p: BlockXYZ)(using chunk: ProtoChunk): Unit =
    p.usingXYZ(chunk.getOrCreateCarvingMask.set)

  def build()(using WorldGenLevel, BlockYBox): Unit =
    if !isWall then
      replaceSoil(actualFloorLevel)
      if isStreamBed then
        airFlow.foreach(surfacePos.above.setBlock)
      if isFallRim then
        surfacePos.scheduleFluidTick()

  private def actualFloorLevel(using BlockGetter, BlockYBox): BlockY =
    floorLevelWithNoise(pos.at(maxFloorLevel).downwards.find(_.isSolidBlock).get.y)

  /** [[https://en.wikipedia.org/wiki/Alluvium alluvium]] */
  private def replaceSoil(floorLevel: BlockY)(using WorldGenLevel, BlockYBox): Unit =
    lazy val floorPos = pos.at(floorLevel) //maxFloorPos.downwards.find(_.isSolid).get
    lazy val (soilStack, rockStack) = floorPos.downwards.span(_.isGranular)
    lazy val rockState = rockStack.headOption.filter(_.isSolidBlock)
      .fold(segment.generator.baseRock)(_.blockState)
    for p <- pos.at(clearLevelWithNoise(minClearLevel)).upwards.takeWhile(_.above.isGranular) do
      p.setBlock(rockState)
    if soilStack.nonEmpty then
      soilStack.tail.foreach(_.setBlock(rockState))
    if floorPos.y <= segment.surfaceLevel then
      lazy val biome = floorPos.biome
      val soilReplacement =
        if segment.surfaceLevel > segment.generator.minSurfaceLevel then
          when(isStreamBed)(Some(if rockState.blockIs(STONE) && !isFallRim then GRAVEL.blockState else rockState))
        else if !floorPos.isGranular then
          when(floorPos.blockIs(STONE))(GRAVEL.blockState) // underground mostly
        else if biome(IS_DESERT) then
          Some(if floorPos.y == segment.surfaceLevel then GRASS_BLOCK.blockState else DIRT.blockState) // floodplains
        else if floorPos.blockIs(SAND) || floorPos.blockIs(GRAVEL) then
          None
        else if biome(IS_HOT) then
          None
        else if biome(IS_COLD) then
          Some(GRAVEL.blockState)
        else
          Some(SAND.blockState)
      soilReplacement.foreach(floorPos.setBlock)

  private def floorLevelWithNoise(baseLevel: BlockY): BlockY =
    if baseLevel == segment.surfaceLevel :- 1 then baseLevel
    else
      val noiseIndex = distance(segment.surfaceLevel, baseLevel).clampZeroTo(FloorNoises.lastIndex)
      baseLevel :+ FloorNoises(noiseIndex).zeroOrOneAt(pos, Probability(0.25))

  private def clearLevelWithNoise(baseLevel: BlockY): BlockY =
    baseLevel :- CeilingNoise.zeroOrOneAt(pos)

end StreamsColumn

def merge(columns: NonEmptySeq[StreamsColumn]): StreamsColumn =
  val cols = columns
  cols.singletonOption.getOrElse:
    val deepestFlows = cols.filter(_.isStreamBed).minsBy(_.maxFloorLevel).flatMap(_.airFlow)
    StreamsColumn(
      cols.uniqueMappingFor(_.segment).get,
      cols.uniqueMappingFor(_.pos).get,
      cols.map(_.maxFloorLevel).min,
      cols.map(_.minClearLevel).max,
      StreamsAirFlowBlock.averageFlowState(deepestFlows),
      cols.exists(_.isFallRim),
      cols.exists(_.isFallBase)
    )

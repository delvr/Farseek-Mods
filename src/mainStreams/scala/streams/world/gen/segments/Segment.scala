package streams.world.gen
package segments

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.util.RandomSource
import streams.blocks.*
import streams.states.*

abstract class Segment(using val noise: NoiseState):
  def generator: StreamsGenerator
  protected def basinPos: BasinXZ

  val random: RandomSource = RandomSource.create(chunkBounds.hashCode)

  // Bounds
  // -----------------------------------------------------------------------------------------------
  protected lazy val chunkBounds: ChunkXZBox
  final lazy val xzBounds: BlockXZBox = chunkBounds.converted
  lazy val interiorBounds: BlockXZBox = (xzBounds.x.contractBy(1), xzBounds.z.contractBy(1))
  lazy val pathSteps: PositiveInt

  // Levels
  // -----------------------------------------------------------------------------------------------
  lazy val surfaceLevel: BlockY = generator.minSurfaceLevel
  lazy val downstreamSurfaceLevel: BlockY = surfaceLevel
  lazy val maxDepth: PositiveInt = generator.maxRiverDepth
  lazy val baseTunnelHeight: PositiveInt = generator.maxTunnelHeight

  // Directions/Neighbors
  // -----------------------------------------------------------------------------------------------
  protected lazy val bendsLeft: Boolean
  final protected def bendsRight: Boolean = !bendsLeft
  protected def neighbor(side: XZSide): Option[Segment]

  // Inlets/Outlets
  // -----------------------------------------------------------------------------------------------
  def outlets: NonEmptySeq[ChannelAtBorder]
  protected lazy val inlets: ISeq[ChannelAtBorder]
  protected def inletOn(side: XZSide): Option[ChannelAtBorder] = neighbor(side).collect:
    case us: UpstreamSegment if us.downstreamSide == -side => copyUpstreamOutlet(us.outlet)
  lazy val outletIsFall: Boolean = downstreamSurfaceLevel < this.surfaceLevel
  private lazy val plungePoolDepth: PositiveInt =
    MinFallHeight.clampBetween(PositiveInt(maxDepth/2), maxDepth)

  private def copyUpstreamOutlet(outlet: ChannelAtBorder): ChannelAtBorder =
    outlet.copy(segment = this, outlet.streamDirection, outlet.slopesLeftToRight.map(col => col.copy(
      segment = this,
      pos = col.pos :+ outlet.streamDirection,
      maxFloorLevel =
        if col.isStreamBed && col.isFallRim then surfaceLevel :- plungePoolDepth
        else col.maxFloorLevel,
      isFallBase = col.isFallRim
    )))

  // Chunk data
  // -----------------------------------------------------------------------------------------------
  lazy val chunkData: MapWithInvalidation[ChunkXZ, StreamsChunk] =
    generateChunkData.withInvalidation

  def invalidate(p: into[ChunkXZ]): Unit = chunkData.invalidate(p)

  def generateChunkData: Map[ChunkXZ, StreamsChunk] =
    val borderColumns = inlets.flatMap(_.slopesLeftToRight) ++ outlets.flatMap(_.slopesLeftToRight)
    val pathColumns = for
      inlet  <- inlets
      outlet <- outlets
      channel = channelBetween(inlet, outlet).map(transform)
      step   <- interpolatedSteps(channel.flatten.assumedNonEmpty)
      if !step.isWall && step.pos.isIn(xzBounds)
    yield step
    (borderColumns ++ pathColumns)
      .groupBy(_.pos).mapVals(merge).groupBy(_._1.into[ChunkXZ]).mapVals(StreamsChunk(this, _))

  // Channels
  // -----------------------------------------------------------------------------------------------
  type Path = NonEmptySeq[StreamsColumn]

  lazy val forwardSkew : `[0,1]` = random.between(`[0,1]`(0.1), `1.0`)
  lazy val backwardSkew: `[0,1]` = random.between(`[0,1]`(0.1), `1.0`)

  private def interpolatedSteps(channelSteps: NonEmptySeq[StreamsColumn]): NonEmptySeq[StreamsColumn] =
    val occupied = channelSteps.map(_.pos).toSet
    (channelSteps ++ channelSteps.flatMap: col =>
      col.pos.neighbors4.filterNot(occupied).flatMap: nPos =>
        when(occupied(nPos :+ North) && occupied(nPos :+ South) ||
             occupied(nPos :+ East ) && occupied(nPos :+  West)
        )(col.copy(pos = nPos))).assumedNonEmpty

  private def channelBetween(inlet: ChannelAtBorder, outlet: ChannelAtBorder): ISeq[Path] =
    assert(inlet.segment == this && outlet.segment == this)
    val inletSections  = splitChannel( inlet.slopesLeftToRight)
    val outletSections = splitChannel(outlet.slopesLeftToRight)
    val paths = for
      (inletSection, outletSection) <- inletSections zip outletSections
      (start, end) <- connectionsBetween(inletSection, outletSection)
    yield curveBetween(start, end, inlet, outlet)
    paths

  private def curveBetween(start: StreamsColumn, end: StreamsColumn, inlet: ChannelAtBorder, outlet: ChannelAtBorder): Path =
    if inlet.streamAxis != outlet.streamAxis then quadraticCurve(start, end, inlet)
    else cubicCurve(start, end, inlet, outlet)

  protected def splitChannel(slopesLeftToRight: NonEmptySeq[StreamsColumn]): NonEmptySeq[NonEmptySeq[StreamsColumn]] =
    val ( leftBank, streamBedAndRightBank) = slopesLeftToRight.span(_.maxFloorLevel >= surfaceLevel)
    val (rightBank, streamBed) = streamBedAndRightBank.reverse.span(_.maxFloorLevel >= surfaceLevel)
    NonEmptySeq(leftBank.assumedNonEmpty, streamBed.reverse.assumedNonEmpty, rightBank.reverse.assumedNonEmpty)

  private def connectionsBetween[T](inlet: NonEmptySeq[T], outlet: NonEmptySeq[T]): NonEmptySeq[(T, T)] =
    val connectedIndices: NonEmptySeq[(Index, Index)] =
      val iLen =  inlet.length
      val oLen = outlet.length
      if      iLen > oLen then  inlet.indexes.map(i => i -> Index(i * oLen / iLen))
      else if iLen < oLen then outlet.indexes.map(i => Index(i * iLen / oLen) -> i)
      else    inlet.indexes.map(i => i -> i)
    connectedIndices.map(inlet(_) -> outlet(_))

  private def pathBetween(start: StreamsColumn, end: StreamsColumn, steps: BezierCurve[EuclidXZ]): Path =
    NonEmptySeq.interpolate(pathSteps): t =>
      given Interpolation[BlockY] = BlockY.Interpolation
      val maxFloorLevel = t.lerp(start.maxFloorLevel, end.maxFloorLevel)
      val minClearLevel = t.lerp(start.minClearLevel, end.minClearLevel)
      val airFlow: Option[BlockState] = when(maxFloorLevel <= surfaceLevel):
        steps.unscaledTangentVectorAt(t).whenNonZero: v =>
          StreamsAirFlowBlock(flowSpeed(maxFloorLevel), FlowHeight.max, rotation015FromYaw(v.yaw))
      StreamsColumn(this, steps(t).converted, maxFloorLevel, minClearLevel, airFlow)

  private def quadraticCurve(start: StreamsColumn, end: StreamsColumn, inlet: ChannelAtBorder): Path =
    val (p0, p2) = (start.pos, end.pos)
    val p1 = if inlet.streamAxis == Axis.X then (p2.x, p0.z) else (p0.x, p2.z)
    pathBetween(start, end, QuadraticCurve(p0.center, p1.center, p2.center))

  private def cubicCurve(start: StreamsColumn, end: StreamsColumn, inlet: ChannelAtBorder, outlet: ChannelAtBorder): Path =
    val (p0, p3) = (start.pos, end.pos)
    val (p1, p2) = inlet.streamAxis match
      case Axis.X =>
        val skewSign = if bendsRight then -1 else 1
        given Interpolation[BlockX] = BlockX.Interpolation
        ( forwardSkew.lerp(p0.x, p3.x) :+ (( inlet.midChannel.z delta p0.z) * skewSign), p0.z) ->
        (backwardSkew.lerp(p3.x, p0.x) :+ ((outlet.midChannel.z delta p3.z) * skewSign), p3.z)
      case Axis.Z =>
        val skewSign = if bendsLeft then -1 else 1
        given Interpolation[BlockZ] = BlockZ.Interpolation
        (p0.x,  forwardSkew.lerp(p0.z, p3.z) :+ (( inlet.midChannel.x delta p0.x) * skewSign)) ->
        (p3.x, backwardSkew.lerp(p3.z, p0.z) :+ ((outlet.midChannel.x delta p3.x) * skewSign))
    pathBetween(start, end, CubicCurve(p0.center, p1.center, p2.center, p3.center))

  protected def transform(path: Path): Path = path

  def flowSpeed(@unused bottomLevel: BlockY): PositiveInt = FlowSpeed.max

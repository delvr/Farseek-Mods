package streams.world.gen
package segments

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}

val MinFallHeight: PositiveInt = `3`

sealed abstract class TributaryNode(basin: TributaryBasin)(using NoiseState)
    extends Segment, ChunkSegment, UpstreamSegment:
  override lazy val generator = basin.generator
  override protected def basinPos = basin.basinPos

  /** [[https://en.wikipedia.org/wiki/Spring_(hydrology) spring]] */
  def isSpring: Boolean = upstreamNodes.isEmpty

  protected lazy val maxSurfaceLevelFromUpstream: BlockY

  final lazy val streamSize: PositiveInt =
    upstreamNodes.toSeq.map(_.streamSize).sumOption.getOrElse(`1`)

  final lazy val upstreamNodes: Set[TributaryUpstreamNode] =
    XZSides.flatMap(neighbor).collect {
      case us: TributaryUpstreamNode if us.downstreamNode == this => us }.toSet

  override protected lazy val outletSlopes: NonEmptySeq[(Int, Int)] =
  (if isSpring then NonEmptySeq(2, 1, -1, -1, 0, 1, 2) else streamSize match
    case 1 => NonEmptySeq(3, 1, 0, -1, -1, -1, 0, 0, 1, 1, 3)
    case 2 => NonEmptySeq(3, 1, 0, -1, -2, -2, -1, 0, 0, 1, 1, 3)
    case _ => NonEmptySeq(3, 1, 0, -1, -2, -3, -3, -2, -1, 0, 0, 1, 1, 3)
  ).map(y => if outletIsFall then maxOf(y, -1) else y).map(y => y -> y)

  final override protected def neighbor(side: XZSide): Option[TributaryNode] =
    (chunkPos :+ side).whenIn(basin.chunkBounds)(basin.pathNodes.get).flatten

end TributaryNode

final case class TributaryOutletNode(basin: TributaryBasin, chunkPos: ChunkXZ)(using NoiseState)
    extends TributaryNode(basin):
  override lazy val downstreamSide: XZSide = basin.downstreamSide

  override protected lazy val maxSurfaceLevelFromUpstream: BlockY = generator.minSurfaceLevel

  override protected lazy val outletSlopes: NonEmptySeq[(Int, Int)] =
    NonEmptySeq(5, 3, 1, -1, -2, -3, -4, -4, -3, -2, -1, 0, 1, 3, 5).map(y => y -> y)
end TributaryOutletNode

final case class TributaryUpstreamNode(basin: TributaryBasin, chunkPos: ChunkXZ, maxSurfaceLevelFromSelf: BlockY,
    downstreamSide: XZSide)(using NoiseState) extends TributaryNode(basin):

  override lazy val maxDepth: PositiveInt = generator.maxTributaryDepth

  override lazy val downstreamSurfaceLevel: BlockY = downstreamNode.surfaceLevel

  override lazy val surfaceLevel: BlockY =
    val fallHeight = (downstreamSurfaceLevel delta maxSurfaceLevelFromUpstream) / 2
    downstreamSurfaceLevel :+ (if fallHeight >= MinFallHeight then fallHeight else 0)

  override lazy val baseTunnelHeight: PositiveInt =
    if isSpring then `4` else generator.maxTributaryTunnelHeight

  override protected lazy val maxSurfaceLevelFromUpstream: BlockY =
    (upstreamNodes.map(_.maxSurfaceLevelFromUpstream) + maxSurfaceLevelFromSelf).min

  override protected def inletOn(side: XZSide): Option[ChannelAtBorder] =
    if isSpring then Some(ChannelAtBorder(this,
      downstreamSide, NonEmptySeq(StreamsColumn(this,
        random.elementOf(interiorBounds.elements), surfaceLevel :- 1, surfaceLevel))))
    else super.inletOn(side)

  override protected def splitChannel(slopesLeftToRight: NonEmptySeq[StreamsColumn]): NonEmptySeq[NonEmptySeq[StreamsColumn]] =
    if isSpring then NonEmptySeq(slopesLeftToRight, slopesLeftToRight, slopesLeftToRight)
    else super.splitChannel(slopesLeftToRight)

  lazy val downstreamNode: TributaryNode = neighbor(downstreamSide).get

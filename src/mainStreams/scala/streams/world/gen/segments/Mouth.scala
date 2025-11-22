package streams.world.gen
package segments

import farseek.game.*
import farseek.game.imports.*
import farseek.util.{*, given}

/** A [[https://en.wikipedia.org/wiki/River_mouth river mouth]]. */
final case class Mouth(generator: StreamsGenerator, basinPos: BasinXZ,
    leaksLeftToRight: NonEmptySeq[NonEmptySeq[BlockXZ]])(using NoiseState) extends BasinSegment:

  override protected lazy val inlets: NonEmptySeq[ChannelAtBorder] = NonEmptySeq(inlet)

  private lazy val inlet: ChannelAtBorder = inletOn(East).get

  override lazy val outlets: NonEmptySeq[ChannelAtBorder] = leaksLeftToRight.map: leak =>
    val direction =
      if leak.exists(_.z == xzBounds.minZ) then North
      else if leak.exists(_.z == xzBounds.maxZ) then South
      else West
    val outletWidth = leak.length
    val bankWidth = outletWidth / 3
    val clearLevel = surfaceLevel :+ baseTunnelHeight
    val outletSlopes =
      leak.slice(0, bankWidth).map(p =>
        StreamsColumn(this, p, surfaceLevel, clearLevel)) ++
      leak.slice(bankWidth, outletWidth - bankWidth).map(p =>
        StreamsColumn(this, p, surfaceLevel :- 1, clearLevel)) ++
      leak.slice(outletWidth - bankWidth, outletWidth).map(p =>
        StreamsColumn(this, p, surfaceLevel, clearLevel))
    ChannelAtBorder(this, direction, outletSlopes.assumedNonEmpty)

  override protected def transform(path: Path): Path =
    val inletFloorLevel = path.first.maxFloorLevel
    val straightPath = path.map(_.copy(maxFloorLevel = inletFloorLevel))
    straightPath.firstIndexWhere(step => generator.likelyInSea(step.pos.at(
      if generator.hasLava then generator.minSurfaceLevel else inletFloorLevel.above
    ))) match
        case Some(iLastFlow) => straightPath.indexes.map: i =>
          if i <= iLastFlow then straightPath(i) else straightPath(i).copy(airFlow = None)
        case _ => straightPath

package streams.world.gen
package segments

import farseek.game.{*, given}
import farseek.util.{*, given}

trait UpstreamSegment extends Segment:

  // Downstream/outlets
  // ------------------------------------------------------------------------------------------------------------------
  def downstreamSide: XZSide
  protected lazy val outletOffset: NonNegativeInt = `0`
  protected def outletSlopes: NonEmptySeq[(Int, Int)] // valley floor height, then tunnel floor height
  private def adjustedOutletSlopes = outletSlopes.map: (vh, th) =>
    (if vh < 0 then maxOf(vh, -maxDepth) else vh, if th < 0 then maxOf(th, -maxDepth) else th)
  override lazy val baseTunnelHeight: PositiveInt = PositiveInt(12)
  private lazy val baseClearLevel: BlockY = surfaceLevel :+ baseTunnelHeight

  lazy val outlet: ChannelAtBorder =
    val downstreamEdgeOuterToInner = xzBounds.facet(downstreamSide).transformWhen(
      (downstreamSide == South || downstreamSide == West) == bendsLeft)(_.reverse)
      .drop(outletOffset).assumedNonEmpty
    assert(adjustedOutletSlopes.length <= downstreamEdgeOuterToInner.length)
    var passedOuterBank = false
    var done = false
    val slopesOuterToInner = adjustedOutletSlopes.indexes.flatMap: i =>
      when(!done):
        val pos = downstreamEdgeOuterToInner(i)
        val (valleyFloorHeight, tunnelFloorHeight) = adjustedOutletSlopes(i)
        assert(valleyFloorHeight < 0 == tunnelFloorHeight < 0)
        val floorHeight = if generator.maxSurfaceAt(pos) > baseClearLevel then tunnelFloorHeight else valleyFloorHeight
        val floorLevel =   surfaceLevel :+ floorHeight // floorLevelWithNoise(pos, surfaceLevel + floorHeight)
        val clearLevel = baseClearLevel :- floorHeight/2 // clearLevelWithNoise(pos, baseClearLevel - floorHeight/2)
        val col = StreamsColumn(this, pos, floorLevel, clearLevel, isFallRim = outletIsFall)
        if valleyFloorHeight < 0 then
          passedOuterBank = true
        if passedOuterBank && col.isWall then
          done = true
        col
    val slopesLeftToRight = slopesOuterToInner.assumedNonEmpty.transformWhen(bendsRight)(_.reverse)
    ChannelAtBorder(this, downstreamSide, slopesLeftToRight)

  override lazy val outlets: NonEmptySeq[ChannelAtBorder] = NonEmptySeq(outlet)

  // Upstream/inlets
  // ------------------------------------------------------------------------------------------------------------------
  private lazy val straightInlet: Option[ChannelAtBorder] = inletOn(-downstreamSide)
  private lazy val leftInlet:     Option[ChannelAtBorder] = inletOn( downstreamSide.rotateLeft)
  private lazy val rightInlet:    Option[ChannelAtBorder] = inletOn( downstreamSide.rotateRight)

  override protected lazy val inlets: NonEmptySeq[ChannelAtBorder] =
    Seq(straightInlet, leftInlet, rightInlet).flatten.assumedNonEmpty

package streams.world.gen
package segments

import farseek.game.*
import farseek.util.*

/** A [[https://en.wikipedia.org/wiki/Cross_section_(geometry) cross section]] (looking downstream)
  * of a river [[https://en.wikipedia.org/wiki/Channel_(geography) channel]]. */
final case class ChannelAtBorder(segment: Segment, streamDirection: XZSide,
    slopesLeftToRight: NonEmptySeq[StreamsColumn]):
  def streamAxis: Axis.Horizontal = streamDirection.axis
  lazy val positions: Set[BlockXZ] = slopesLeftToRight.map(_.pos).toSet
  lazy val midChannel: BlockXZ = slopesLeftToRight.lowMid.pos

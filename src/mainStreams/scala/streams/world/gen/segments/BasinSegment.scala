package streams.world.gen
package segments

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.{*, given}
import streams.states.*

abstract class BasinSegment(using NoiseState) extends Segment:

  final override lazy val bendsLeft: Boolean = basinPos.bendsLeft
  final override lazy val chunkBounds: ChunkXZBox = basinPos.converted
  final override lazy val pathSteps = PositiveInt(256)

  final override protected def neighbor(side: XZSide): Option[BasinSegment] =
    generator.getOrCreateSegmentAt(basinPos :+ side)

  final override def flowSpeed(bottomLevel: BlockY): PositiveInt =
    distance(bottomLevel, surfaceLevel).clampOneTo(FlowSpeed.max)

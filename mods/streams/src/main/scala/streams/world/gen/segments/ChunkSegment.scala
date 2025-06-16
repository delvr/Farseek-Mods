package streams.world.gen
package segments

import farseek.game.*
import farseek.util.*

trait ChunkSegment extends Segment:
  def chunkPos: ChunkXZ
  final override lazy val bendsLeft: Boolean = chunkPos.x.isEven == chunkPos.z.isEven
  final override lazy val chunkBounds: ChunkXZBox = (chunkPos.x.singleton, chunkPos.z.singleton)
  final override lazy val pathSteps: PositiveInt = `64`

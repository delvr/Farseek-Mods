package streams.world.gen
package segments

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.*

/** An East-West-flowing [[https://en.wikipedia.org/wiki/Reach_(geography) reach]] of a river's
  * [[https://en.wikipedia.org/wiki/Main_stem main stem]], either a northern or southern
  * [[https://en.wikipedia.org/wiki/Meander meander]].*/
final case class Reach(generator: StreamsGenerator, basinPos: BasinXZ)(using NoiseState)
    extends BasinSegment, UpstreamSegment:

  override lazy val downstreamSide: XZSide = West

  override protected lazy val outletOffset: NonNegativeInt = random.zeroTo(PositiveInt(30))

  override protected lazy val outletSlopes: NonEmptySeq[(Int, Int)] = NonEmptySeq(
    (5, 7), (3, 5), (1, 3), (0, 1), (-1, -1), (-2, -3), (-3, -5), (-4, -6), (-5, -7), (-6, -8), (-6, -8), (-7, -8),
    (-7, -8), (-7, -8), (-6, -7), (-6, -6), (-5, -5), (-4, -4), (-3, -3), (-2, -2), (-1, -1), (-1, -1), (0, 0),
    (0, 1), (0, 3), (0, 5), (0, 8), (1, 12), (1, 12), (1, 12), (2, 12), (2, 12), (3, 12), (4, 12), (5, 12))

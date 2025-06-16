package farseek.game

import farseek.util.{*, given}
import farseek.util.imports.*

extension[Y: IntShift](y: Y)
  def above: Y = y :+ 1
  def below: Y = y :- 1

extension[Y: IntShift](y: into Y)(using GridBox[Y])
  def optionAbove: Option[Y] = y.above.someWhenInBounds
  def optionBelow: Option[Y] = y.below.someWhenInBounds

extension[X, Y: IntShift, Z](p: into (X, Z))
  def above(y: Y): (X, Y, Z) = (p.x, y.above, p.z)
  def below(y: Y): (X, Y, Z) = (p.x, y.below, p.z)

extension[X, Y: IntShift, Z](p: into (X, Z))(using GridBox[Y])
  def optionAbove(y: Y): Option[(X, Y, Z)] = y.optionAbove.map(p.at)
  def optionBelow(y: Y): Option[(X, Y, Z)] = y.optionBelow.map(p.at)

extension[X: IntShift, Z: IntShift](p: into (X, Z))
  def neighbors4    : NonEmptySeq[(X, Z)] = XZSides.map(p :+ _)
  def neighbors8    : NonEmptySeq[(X, Z)] = neighbors4 +++ ordinalNeighbors
  def withNeighbors4: NonEmptySeq[(X, Z)] = p +:: neighbors4
  def withNeighbors8: NonEmptySeq[(X, Z)] = p +:: neighbors8
  def ordinalNeighbors: NonEmptySeq[(X, Z)] = NonEmptySeq(
    p :+ North :+ West, p :+ North :+ East, p :+ South :+ West, p :+ South :+ East)

extension[X, Y: IntShift, Z](p: into (X, Y, Z))
  def above: (X, Y, Z) = (p.x, p.y.above, p.z)
  def below: (X, Y, Z) = (p.x, p.y.below, p.z)
  def neighbors2    : NonEmptySeq[(X, Y, Z)] = NonEmptySeq(above, below)
  def withNeighbors2: NonEmptySeq[(X, Y, Z)] = p +:: neighbors2

extension[X, Y: IntShift, Z](p: into (X, Y, Z))(using GridBox[Y])
  def optionAbove: Option[(X, Y, Z)] = p.y.optionAbove.map(p.at)
  def optionBelow: Option[(X, Y, Z)] = p.y.optionBelow.map(p.at)

extension[X: IntShift, Y, Z: IntShift](p: into (X, Y, Z))
  def neighbors4    : NonEmptySeq[(X, Y, Z)] = p.xz.neighbors4.map(_.at(p.y))
  def withNeighbors4: NonEmptySeq[(X, Y, Z)] = p +:: neighbors4

extension[X: IntShift, Y: IntShift, Z: IntShift](p: into (X, Y, Z))
  def neighbors6    : NonEmptySeq[(X, Y, Z)] = p.neighbors4 +++ p.neighbors2
  def withNeighbors6: NonEmptySeq[(X, Y, Z)] = p +:: neighbors6

extension[Y](box: into GridBox[Y])
  def upwards  : NonEmptySeq[Y] = box
  def downwards: NonEmptySeq[Y] = upwards.reverse

extension[X, Z](box: into XZGridBox[X, Z])
  def xCount: PositiveInt = box.x.length
  def zCount: PositiveInt = box.z.length
  def eastwards : NonEmptySeq[X] = box.x
  def westwards : NonEmptySeq[X] = eastwards.reverse
  def southwards: NonEmptySeq[Z] = box.z
  def northwards: NonEmptySeq[Z] = southwards.reverse
  def elements: NonEmptySeq[(X, Z)] =
    (for { x <- box.x; z <- box.z } yield (x, z)).assumedNonEmpty
  def facet(side: XZSide): NonEmptySeq[(X, Z)]  = facets.apply(side)
  def facets: Map[XZSide,  NonEmptySeq[(X, Z)]] = XZSides.mappedTo:
    case North => box.x.map((_, box.minZ))
    case South => box.x.map((_, box.maxZ))
    case West  => box.z.map((box.minX, _))
    case East  => box.z.map((box.maxX, _))
  def borderElements: NonEmptySeq[(X, Z)] =
    if xCount == 1 || zCount == 1 then elements
    else facet(North) ::++ facet(South) ::++ facet(West).interior ::++ facet(East).interior

extension[Y: {Enumerable, Ordering}](p: Y)
  def upwards  (using box: GridBox[Y]): IndexedSeqView[Y] = upwardsTo(box.max)
  def downwards(using box: GridBox[Y]): IndexedSeqView[Y] = downwardsTo(box.min)
  infix def upwardsTo     (top:    Y ): IndexedSeqView[Y] = p forwardsTo top
  infix def downwardsTo   (bottom: Y ): IndexedSeqView[Y] = p backwardsTo bottom
  infix def upwardsUntil  (top:    Y ): IndexedSeqView[Y] = upwardsTo(top).dropRight(1)
  infix def downwardsUntil(bottom: Y ): IndexedSeqView[Y] = downwardsTo(bottom).dropRight(1)

extension[X, Y: {Enumerable, Ordering}, Z](p: into (X, Y, Z))
  def upwards  (using box: GridBox[Y]): IndexedSeqView[(X, Y, Z)] = upwardsTo(box.max)
  def downwards(using box: GridBox[Y]): IndexedSeqView[(X, Y, Z)] = downwardsTo(box.min)
  infix def upwardsTo     (top:    Y ): IndexedSeqView[(X, Y, Z)] = (p.y upwardsTo top).map(p.xz.at)
  infix def downwardsTo   (bottom: Y ): IndexedSeqView[(X, Y, Z)] = (p.y downwardsTo bottom).map(p.xz.at)
  infix def upwardsUntil  (top:    Y ): IndexedSeqView[(X, Y, Z)] = upwardsTo(top).dropRight(1)
  infix def downwardsUntil(bottom: Y ): IndexedSeqView[(X, Y, Z)] = downwardsTo(bottom).dropRight(1)

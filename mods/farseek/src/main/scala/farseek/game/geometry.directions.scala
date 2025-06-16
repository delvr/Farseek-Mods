package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.DirectionOnAxis.*

sealed trait Axis
object Axis:
  sealed trait Horizontal extends Axis:
    def rotated: Horizontal = this match
      case X => Z
      case Z => X
  sealed trait Vertical extends Axis

  case object X extends Horizontal
  case object Z extends Horizontal
  case object Y extends Vertical

  given Left90Rotation [Horizontal] = _.rotated
  given Right90Rotation[Horizontal] = _.rotated
end Axis

import farseek.game.Axis.*

sealed trait Side:
  def axis: Axis
  def direction: DirectionOnAxis

enum XZSide(val axis: Horizontal, val direction: DirectionOnAxis) extends Side:
  case North extends XZSide(Z, Negative)
  case South extends XZSide(Z, Positive)
  case West  extends XZSide(X, Negative)
  case East  extends XZSide(X, Positive)

enum YSide(val axis: Vertical, val direction: DirectionOnAxis) extends Side:
  case Down  extends YSide(Y, Negative)
  case Up    extends YSide(Y, Positive)

export farseek.game.XZSide.*
export farseek.game.YSide.*

type XYZSide = XZSide | YSide

val YSides:   NonEmptySeq[  YSide] = NonEmptySeq.from(YSide.values)
val XZSides:  NonEmptySeq[ XZSide] = NonEmptySeq.from(XZSide.values)
val XYZSides: NonEmptySeq[XYZSide] = XZSides +++ YSides

given Negation[XZSide] =
  case North => South
  case South => North
  case West  => East
  case East  => West

given Negation[YSide] =
  case Down  => Up
  case Up    => Down

given Left90Rotation[XZSide] =
  case North => West
  case South => East
  case West  => South
  case East  => North

given Right90Rotation[XZSide] =
  case North => East
  case South => West
  case West  => North
  case East  => South

given Direction Into XYZSide =
  case NORTH => North
  case SOUTH => South
  case WEST  => West
  case EAST  => East
  case DOWN  => Down
  case UP    => Up

given XZSide Into Direction =
  case North => NORTH
  case South => SOUTH
  case West  => WEST
  case East  => EAST

given XYZSide Into Direction =
  case North => NORTH
  case South => SOUTH
  case West  => WEST
  case East  => EAST
  case Down  => DOWN
  case Up    => UP

given YSide Into Int =
  case Down  => -1
  case Up    => +1

given XZSide Into (Int, Int) =
  case North => (0, -1)
  case South => (0, +1)
  case West  => (-1, 0)
  case East  => (+1, 0)

given XYZSide Into (Int, Int, Int) =
  case North => (0, 0, -1)
  case South => (0, 0, +1)
  case West  => (-1, 0, 0)
  case East  => (+1, 0, 0)
  case Down  => (0, -1, 0)
  case Up    => (0, +1, 0)

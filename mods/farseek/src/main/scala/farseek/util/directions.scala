package farseek.util

import farseek.util.imports.*

enum DirectionOnAxis extends Ordered[DirectionOnAxis]:
  case Negative, Origin, Positive
  override def compare(that: DirectionOnAxis): Int = this.ordinal compare that.ordinal

object DirectionOnAxis:
  given Negation[DirectionOnAxis] =
    case Negative => Positive
    case Origin   => Origin
    case Positive => Negative

// counter-clockwise
@sam trait Left90Rotation[T] extends (T => T):
  extension(x: T) def rotateLeft: T = apply(x)

// clockwise
@sam trait Right90Rotation[T] extends (T => T):
  extension(x: T) def rotateRight: T = apply(x)

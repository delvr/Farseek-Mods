package farseek.util

import farseek.util.imports.*

extension[T <: Ord, Ord: PartialOrdering](x: T)
  def < (y: T): Boolean = Ord.lt(x, y)
  def > (y: T): Boolean = Ord.gt(x, y)
  def <=(y: T): Boolean = Ord.lteq(x, y)
  def >=(y: T): Boolean = Ord.gteq(x, y)
  def isBetweenExclusive(a: T, b: T): Boolean = a <  x && x <  b
  def isBetweenInclusive(a: T, b: T): Boolean = a <= x && x <= b

extension[T: Ordering](x: T)
  def clampToMin(a: T): T = if x < a then a else x
  def clampToMax(b: T): T = if x > b then b else x
  def clampBetween(a: T, b: T): T =
    require(a <= b); if x < a then a else if x > b then b else x

def minOf[T: Ordering](x: T, xs: T*): T = (x +: xs).min
def maxOf[T: Ordering](x: T, xs: T*): T = (x +: xs).max

/** The [[https://en.wikipedia.org/wiki/Inclusion_order inclusion order]] of subsets. */
def inclusionOrder[S: Inclusion]: PartialOrderLtEq[S] = _ subsetOf _

/** A single-abstract-method convenience subtrait of [[PartialOrdering]]. */
@sam trait PartialOrderLtEq[T] extends PartialOrdering[T]:
  final override def tryCompare(x: T, y: T): Option[Int] =
    val `x<=y` = lteq(x, y)
    val `y<=x` = lteq(y, x)
    when(`x<=y` || `y<=x`)(if `x<=y` && !`y<=x` then -1 else if !`x<=y` && `y<=x` then +1 else 0)

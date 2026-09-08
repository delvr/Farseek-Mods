package farseek.util

import farseek.util.imports.*

val NonZeroMsg = "non-zero"

@sam trait ZeroTest[-T] extends (T => Boolean):
  extension(x: T)
    def isZero: Boolean = apply(x)
    def nonZero: Boolean = !isZero
    def assertNonZero (desc: String = DefaultDesc): Unit = x.assert (_.nonZero, NonZeroMsg, desc)
    def requireNonZero(desc: String = DefaultDesc): Unit = x.require(_.nonZero, NonZeroMsg, desc)

opaque type NonZero[+T] <: T = T
given ZeroTest[NonZero[Nothing]] = _ => false

def nonZero[T: ZeroTest]: T IntoOption NonZero[T] = Refinement(_.nonZero, NonZeroMsg)

extension[T: ZeroTest](x: T)
  def assumedNonZero: NonZero[T] = x.assumed(nonZero)
  def nonZeroOption: Option[NonZero[T]] = x.asOption(nonZero)
  def whenNonZero[R](f: NonZero[T] => R): Option[R] = nonZeroOption.map(f)

given numericZero: [T: Numeric] => ZeroTest[T] = _ == T.zero

given nonZeroScaling: [V, S] => (scale: V ScaledBy S) => NonZero[V] ScaledBy NonZero[S] = scale
// e.g. V MeasuredBy NonNegativeReal => NonZero[V] MeasuredBy PositiveReal
given nonZeroMeasure: [D, L] => (measure: D MeasuredBy L) => NonZero[D] MeasuredBy NonZero[L] = measure

package farseek.util

import farseek.util.imports.*

@sam infix trait MeasuredBy[-D, +L] extends (D => L):
  extension(d: D) def length: L = apply(d)

def distance[P, D, L](p: P, q: P)(using P SeparatedBy D, D MeasuredBy L): L = (p delta q).length

extension[V](v: V)(using V MeasuredBy NonZeroReal, V ScaledBy NonZeroReal)
  def normalized: V = v :/ v.length

type  IntNorm[T] = T MeasuredBy NonNegativeInt
type RealNorm[T] = T MeasuredBy NonNegativeReal

given  IntNorm[ Int] = x => NonNegativeInt (abs(x))
given RealNorm[Real] = x => NonNegativeReal(abs(x))

// https://en.wikipedia.org/wiki/Euclidean_norm

given RealNorm[Real2] = v => NonNegativeReal(sqrt(v._1*v._1 + v._2*v._2))
given RealNorm[Real3] = v => NonNegativeReal(sqrt(v._1*v._1 + v._2*v._2 + v._3*v._3))

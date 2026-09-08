package farseek.util

import farseek.util.*
import farseek.util.imports.*

/** [[https://en.wikipedia.org/wiki/Linear_interpolation linear interpolation]] */
@sam trait Interpolation[P] extends ((`[0,1]`, P, P) => P):
  protected def lerp(t: `[0,1]`, p0: P, p1: P): P
  final override def apply(t: `[0,1]`, p0: P, p1: P): P =
    if t == 0 then p0 else if t == 1 then p1 else if p0 == p1 then p0 else lerp(t, p0, p1)

extension(t: `[0,1]`) def lerp[P: Interpolation](p0: into[P], p1: into[P]): P = P(t, p0, p1)

def midpoint[P: Interpolation](p0: P, p1: P): P = `0.5`.lerp(p0, p1)

/** A [[https://en.wikipedia.org/wiki/Directed_line_segment directed line segment]]. */
open class DirectedLineSegment[+P: Interpolation](val a: P, val b: P):
  override def toString: String = s"${a.toString} --> ${b.toString}"
  final def lerpAt(t: `[0,1]`): P = t.lerp(a, b)
  final def center: P = midpoint(a, b)

extension[P, V](dls: DirectedLineSegment[P])(using P SeparatedBy V)
  def vector: V = dls.a delta dls.b
  def length[L](using V MeasuredBy L): L = vector.length

extension[P: Interpolation](a: P)
  def -->(b: P): DirectedLineSegment[P] = DirectedLineSegment(a, b)

given [P, V] => (P SeparatedBy V) => DirectedLineSegment[P] Into V = _.vector

/** A [[https://en.wikipedia.org/wiki/B%C3%A9zier_curve Bézier curve]]. */
trait BezierCurve[+P](val degree: PositiveInt) extends (`[0,1]` => P):
  def tangentSegmentAt(t: `[0,1]`): DirectedLineSegment[P]
  final override def apply(t: `[0,1]`): P = tangentSegmentAt(t).lerpAt(t)

/** A [[https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_B%C3%A9zier_curves quadratic Bézier curve]]. */
final case class QuadraticCurve[P: Interpolation](p0: P, p1: P, p2: P) extends BezierCurve[P](`2`):
/** [[https://en.wikipedia.org/wiki/De_Casteljau%27s_algorithm De Casteljau's algorithm]] */
  override def tangentSegmentAt(t: `[0,1]`): DirectedLineSegment[P] = t.lerp(p0, p1) --> t.lerp(p1, p2)

/** A [[https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B%C3%A9zier_curves cubic Bézier curve]]. */
final case class CubicCurve[P: Interpolation](p0: P, p1: P, p2: P, p3: P) extends BezierCurve[P](`3`):
  /** [[https://en.wikipedia.org/wiki/De_Casteljau%27s_algorithm De Casteljau's algorithm]] */
  override def tangentSegmentAt(t: `[0,1]`): DirectedLineSegment[P] = derivativeAt(t).tangentSegmentAt(t)
  def derivativeAt(t: `[0,1]`) = QuadraticCurve(t.lerp(p0, p1), t.lerp(p1, p2), t.lerp(p2, p3))

extension[P, V](curve: BezierCurve[P])(using P SeparatedBy V)
  def unscaledTangentVectorAt(t: `[0,1]`): V = curve.tangentSegmentAt(t).vector
  def tangentVectorAt(t: `[0,1]`)(using V ScaledBy Real): V =
    unscaledTangentVectorAt(t) :* curve.degree.toReal

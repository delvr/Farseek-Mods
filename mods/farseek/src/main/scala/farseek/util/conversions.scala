package farseek.util

import farseek.util.imports.*

// Warning: avoid declaring `given` functions (especially [[PartialFunction]]s) as they can interfere
// with the [[Predef.$conforms]] identity used by [[IterableOnceOps.flatMap]].

/** [[https://dotty.epfl.ch/docs/reference/experimental/into-modifier "into" modifier]] */
infix type Into[A, B] = Conversion[A, B]
extension[A](a: A)
  def into[B](using convert: A Into B): B = convert(a)

infix type IntoOption[A, B] = PartialFunction[A, B]

extension[A, B](pf: A IntoOption B)
  def option(a: A): Option[B] = pf.unapply(a)

extension[A, B](a: A)
  def assumed (pf: A IntoOption B): B = pf(a)
  def asOption(pf: A IntoOption B): Option[B] = pf.option(a)

class PartialConversion[-A, +B](p: A => Boolean, convert: A => B, val mustBe: String)
    extends PartialFunction[A, B]:
  final override def isDefinedAt(a: A): Boolean = p(a)
  final override def apply(a: A): B = convert(this.validated(a))
  final override def toString = mustBe

class MinConversion[-A: Ordering, +B](min: A, convert: A => B) extends PartialConversion[A, B](
    _ >= min, convert, s">= ${min.toString}"):
  def clamp(a: A): B = convert(a.clampToMin(min))
  def clamp(a: A, max: A): B = convert(a.clampBetween(min, max))

class MinMaxConversion[-A: Ordering, +B](min: A, max: A, convert: A => B) extends PartialConversion[A, B](
    _.isBetweenInclusive(min, max), convert, s"within [${min.toString}, ${max.toString}]"):
  def clamp(a: A): B = convert(a.clampBetween(min, max))

infix type IntoOptionWithMin   [A, B] =    MinConversion[A, B]
infix type IntoOptionWithMinMax[A, B] = MinMaxConversion[A, B]

/** A [[https://en.wikipedia.org/wiki/Refinement_type refinement type]] for
* [[https://docs.scala-lang.org/scala3/reference/other-new-features/opaques.html opaque type aliases]]. */
class Refinement[T](validation: T => Boolean, mustBe: String)
    extends PartialConversion[T, T](validation, identity, mustBe)

given Boolean Into JBoolean = java.lang.Boolean.valueOf
given Int     Into JInteger = java.lang.Integer.valueOf
given Long    Into JLong    = java.lang.Long.valueOf
given Float   Into JFloat   = java.lang.Float.valueOf
given Double  Into JDouble  = java.lang.Double.valueOf

given JBoolean Into Boolean = _.booleanValue
given JInteger Into Int     = _.intValue
given JLong    Into Long    = _.longValue
given JFloat   Into Float   = _.floatValue
given JDouble  Into Double  = _.doubleValue

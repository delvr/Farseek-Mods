package farseek.util

import farseek.util.imports.*

/** [[https://docs.scala-lang.org/scala3/reference/experimental/into.html "into" modifier]] */
infix type Into[A, B] = Conversion[A, B]
extension[A](a: A)
  def into[B](using convert: A Into B): B = convert(a)
extension[A, B](a: A)(using A Into B)
  def converted: B = a.into[B] // usable in cases where B (and A Into B) is unambiguous

infix type IntoOption[A, B] = PartialFunction[A, B]

extension[A, B](pf: A IntoOption B)
  def option(a: A): Option[B] = pf.unapply(a)

// [[PartialFunction]] utilities take explicit `pf` arguments since declaring PartialFunctions as givens
// can interfere with the implicit [[Predef.$conforms]] identity used by [[IterableOnceOps.flatMap]].
extension[A, B](a: A)
  def assumed (pf: A IntoOption B): B = pf(a)
  def asOption(pf: A IntoOption B): Option[B] = pf.option(a)

class PartialConversion[-A, +B](p: A => Boolean, convert: A => B, val mustBe: String)
    extends PartialFunction[A, B]:
  final override def isDefinedAt(a: A): Boolean = p(a)
  final override def apply(a: A): B = convert(this.validated(a))
  final override def toString = mustBe

class MinConversion[-A: Ordering, +B](min: A, convert: A => B)
    extends PartialConversion[A, B](_ >= min, convert, ">= " + min):
  def clamp(a: A): B = convert(a.clampToMin(min))
  def clamp(a: A, max: A): B = convert(a.clampBetween(min, max))

class MinMaxConversion[-A, +B](interval: Interval[A], convert: A => B)
    extends PartialConversion[A, B](interval.contains, convert, "within " + interval):
  def clamp(a: A): B = convert(interval.clamp(a))

infix type IntoOptionWithMin   [A, B] =    MinConversion[A, B]
infix type IntoOptionWithMinMax[A, B] = MinMaxConversion[A, B]

/** A [[https://en.wikipedia.org/wiki/Refinement_type refinement type]] for
* [[https://docs.scala-lang.org/scala3/reference/other-new-features/opaques.html opaque type aliases]]. */
class Refinement[T](validation: T => Boolean, mustBe: String)
    extends PartialConversion[T, T](validation, identity, mustBe)

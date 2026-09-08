package farseek.util

import farseek.util.imports.*

val NonEmptyMsg = "non-empty"

@sam trait EmptyTest[-T] extends (T => Boolean):
  extension(x: T)
    def isEmpty: Boolean = apply(x)
    def nonEmpty: Boolean = !isEmpty
    def assertNonEmpty (desc: String = DefaultDesc): Unit = x.assert (_.nonEmpty, NonEmptyMsg, desc)
    def requireNonEmpty(desc: String = DefaultDesc): Unit = x.require(_.nonEmpty, NonEmptyMsg, desc)

opaque type NonEmpty[+T] <: T = T
given EmptyTest[NonEmpty[Nothing]] = _ => false

def nonEmpty[T: EmptyTest]: T IntoOption NonEmpty[T] = Refinement(_.nonEmpty, NonEmptyMsg)

extension[T: EmptyTest](x: T)(using @unused T: T NotA AnySeq[?])
  def assumedNonEmpty: NonEmpty[T] = x.assumed(nonEmpty)
  def nonEmptyOption: Option[NonEmpty[T]] = x.asOption(nonEmpty)
  def whenNonEmpty[R](f: NonEmpty[T] => R): Option[R] = nonEmptyOption.map(f)

extension[T](xs: AnySeq[T])
  def assumedNonEmpty: NonEmptySeq[T] = xs.assumed(nonEmptySeq)
  def nonEmptyOption: Option[NonEmptySeq[T]] = xs.asOption(nonEmptySeq)
  def whenNonEmpty[R](f: NonEmptySeq[T] => R): Option[R] = nonEmptyOption.map(f)

given EmptyTest[String] = _.isEmpty
given EmptyTest[Option[?]] = _.isEmpty
given EmptyTest[AnyIterable[?]] = _.isEmpty
given EmptyTest[JCollection[?]] = _.isEmpty

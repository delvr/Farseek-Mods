package farseek.util

import farseek.util.imports.*

/** An [[ISeq]] that has at least one element. */
// Note: does _not_ override `C` or `CC[_]` from [[IterableOps]] because not all inherited operations
// preserve the non-empty invariant unless overridden as such.
open class NonEmptySeq[+A] protected[util](_wrapped: IndexedSeq[A]) extends ISeq[A](_wrapped):
  wrapped.assertNonEmpty()

  override protected def className: String = "NonEmptySeq"

  override lazy val length: PositiveInt = PositiveInt(wrapped.length)
  override def knownSize: PositiveInt = length

  final lazy val sizeReciprocal: `(0,1]` = fraction(`1`, length)

  final override def isEmpty: Boolean = false

  override def indexes: NonEmptySeq[Index] = super.indexes.assumedNonEmpty

  final def lastIndex:    Index = length.minusOne
  final def lowMidIndex:  Index = Index(lastIndex / 2)
  final def highMidIndex: Index = Index(length    / 2)

  final def lowMid:  A = apply(lowMidIndex)
  final def highMid: A = apply(highMidIndex)

  final def sum    [B >: A:       Addition]: B = wrapped.reduce(_ + _)
  final def product[B >: A: Multiplication]: B = wrapped.reduce(_ * _)
  final def average[B >: A: Addition](using B ScaledBy `(0,1]`): B = sum :* sizeReciprocal

  final def findOrLast(p: A => Boolean): A = find(p).getOrElse(last)

  final def interior: ISeq[A] = init.tail

  final def +++[B >: A](b: NonEmptySeq[B]): NonEmptySeq[B] = concat(b).assumedNonEmpty

  final def ::++[B >: A](suffix: IterableOnce[B]): NonEmptySeq[B] =  appendedAll(suffix)
  final def ++::[B >: A](prefix: IterableOnce[B]): NonEmptySeq[B] = prependedAll(prefix)

  override def minsBy[B: Ordering](f: A => B): NonEmptySeq[A] =
    val b = f(minBy(f)); filter(f(_) == b).assumedNonEmpty
  override def maxesBy[B: Ordering](f: A => B): NonEmptySeq[A] =
    val b = f(maxBy(f)); filter(f(_) == b).assumedNonEmpty

  override def mapInto[B](using A Into B): NonEmptySeq[B] =
    super.mapInto[B].assumedNonEmpty
  override def map[B](f: A => B): NonEmptySeq[B] =
    super.map(f).assumedNonEmpty
  override def padTo[B >: A](len: Int, elem: B): NonEmptySeq[B] =
    super.padTo(len, elem).assumedNonEmpty
  override def reverse: NonEmptySeq[A] =
    super.reverse.assumedNonEmpty
  override def tapEach[U](f: A => U): NonEmptySeq[A] =
    super.tapEach(f).assumedNonEmpty

  override def distinctBy[B](f: A => B): NonEmptySeq[A] =
    super.distinctBy(f).assumedNonEmpty
  override def distinct: NonEmptySeq[A] =
    super.distinct.assumedNonEmpty

  override def appendedAll[B >: A](suffix: IterableOnce[B]): NonEmptySeq[B] =
    super.appendedAll(suffix).assumedNonEmpty
  override def prependedAll[B >: A](prefix: IterableOnce[B]): NonEmptySeq[B] =
    super.prependedAll(prefix).assumedNonEmpty

  override def sorted[B >: A: Ordering]: NonEmptySeq[A] =
    super.sorted.assumedNonEmpty
  override def sortBy[B: Ordering](f: A => B): NonEmptySeq[A] =
    super.sortBy(f).assumedNonEmpty
  override def sortWith(lt: (A, A) => Boolean): NonEmptySeq[A] =
    super.sortWith(lt).assumedNonEmpty

  override infix def zip[B](that: IterableOnce[B]): NonEmptySeq[(A, B)] =
    super.zip(that).assumedNonEmpty
  override def zipAll[A1 >: A, B](that: Iterable[B], thisElem: A1, thatElem: B): NonEmptySeq[(A1, B)] =
    super.zipAll(that, thisElem, thatElem).assumedNonEmpty
  override def zipWithIndex: NonEmptySeq[(A, Index)] =
    super.zipWithIndex.assumedNonEmpty
  override def mapWithIndex[B](f: (A, Index) => B): NonEmptySeq[B] =
    super.mapWithIndex(f).assumedNonEmpty

  override def unzip[A1, A2](using A => (A1, A2)): (NonEmptySeq[A1], NonEmptySeq[A2]) =
    val (a1, a2) = super.unzip
    (a1.assumedNonEmpty, a2.assumedNonEmpty)
  override def unzip3[A1, A2, A3](using A => (A1, A2, A3)): (NonEmptySeq[A1], NonEmptySeq[A2], NonEmptySeq[A3]) =
    val (a1, a2, a3) = super.unzip3
    (a1.assumedNonEmpty, a2.assumedNonEmpty, a3.assumedNonEmpty)
end NonEmptySeq

def nonEmptySeq[T]: AnySeq[T] IntoOption NonEmptySeq[T] =
  PartialConversion(_.nonEmpty, NonEmptySeq.from, NonEmptyMsg)

object NonEmptySeq:
  def apply[T](head: T, tail: T*): NonEmptySeq[T] = from(head +: tail)

  def from[T](xs: AnyIterable[T]): NonEmptySeq[T] = xs match
    case seq: NonEmptySeq[T] => seq
    case _ => new NonEmptySeq(xs.indexed)

  def from[T](xs: Array[T]): NonEmptySeq[T] = from(xs.toSeq)

  def fill[T](n: PositiveInt)(elem: => T): NonEmptySeq[T] = ISeq.fill(n)(elem).assumedNonEmpty

  def tabulate[T](n: PositiveInt)(f: Index => T): NonEmptySeq[T] =
    ISeq.tabulate(n)(i => f(Index(i))).assumedNonEmpty

  def interpolate[T](n: PositiveInt)(f: `[0,1]` => T): NonEmptySeq[T] =
    PositiveInt.option(n - 1).fold(NonEmptySeq(f(`0.0`))): iLast =>
      tabulate(n)(i => f(fraction(i, iLast)))

  def lerp(n: PositiveInt)(start: Real, end: Real): NonEmptySeq[Real] =
    interpolate(n)(_.lerp(start, end))
end NonEmptySeq

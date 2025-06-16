package farseek.util

import farseek.util.imports.*
import scala.annotation.unchecked.*
import scala.collection.immutable.{IndexedSeqOps, StrictOptimizedSeqOps}
import scala.collection.mutable.Builder
import scala.collection.{IterableFactoryDefaults, SeqFactory, StrictOptimizedSeqFactory}

/** A wrapper around an [[IndexedSeq]] whose methods return [[Refinement]] types where applicable. */
// See also: https://docs.scala-lang.org/overviews/core/custom-collections.html
open class ISeq[+A] protected(private[util] val wrapped: IndexedSeq[A])
    extends IndexedSeq[A], IterableFactoryDefaults[A, ISeq],
            IndexedSeqOps[A, ISeq, ISeq[A]], StrictOptimizedSeqOps[A, ISeq, ISeq[A]]:

  override protected def className: String = "ISeq"

  override lazy val length: NonNegativeInt = NonNegativeInt(wrapped.length)
  override def knownSize: NonNegativeInt = length

  final override def apply(i: Int): A = wrapped(i)

  def indexes: ISeq[Index] = wrapped.indices.indexed.map(Index(_))

  def minsBy[B: Ordering](f: A => B): ISeq[A] = minByOption(f).fold(EmptySeq): a =>
    val b = f(a); filter(f(_) == b)
  def maxesBy[B: Ordering](f: A => B): ISeq[A] = maxByOption(f).fold(EmptySeq): a =>
    val b = f(a); filter(f(_) == b)

  def mapInto[B](using A Into B): ISeq[B] = map(_.into[B])
  def mapWithIndex[B](f: (A, Index) => B): ISeq[B] = zipWithIndex.map(f(_, _))
  override def zipWithIndex: ISeq[(A, Index)] = super.zipWithIndex.map((a, i) => a -> Index(i))

  final override def count(p: A => Boolean): NonNegativeInt =
    NonNegativeInt(super.count(p))
  final override def segmentLength(p: A => Boolean, from: Int): NonNegativeInt =
    NonNegativeInt(super.segmentLength(p, from))

  final def ::+[B >: A](elem: B): NonEmptySeq[B] = appended (elem)
  final def +::[B >: A](elem: B): NonEmptySeq[B] = prepended(elem)

  final override def appended[B >: A](elem: B): NonEmptySeq[B] =
    super.appended(elem).assumedNonEmpty
  final override def prepended[B >: A](elem: B): NonEmptySeq[B] =
    super.prepended(elem).assumedNonEmpty
  final override def updated[B >: A](i: Int, elem: B): NonEmptySeq[B] =
    super.updated(i, elem).assumedNonEmpty

  final override def scan[B >: A](z: B)(op: (B, B) => B): NonEmptySeq[B] =
    super.scan(z)(op).assumedNonEmpty
  final override def scanLeft [B](z: B)(op: (B, A) => B): NonEmptySeq[B] =
    super.scanLeft(z)(op).assumedNonEmpty
  final override def scanRight[B](z: B)(op: (A, B) => B): NonEmptySeq[B] =
    super.scanRight(z)(op).assumedNonEmpty

  final override def groupBy[K](f: A => K): Map[K, NonEmptySeq[A]] =
    super.groupBy(f).mapVals(_.assumedNonEmpty)
  final override def groupMap[K, B](key: A => K)(f: A => B): Map[K, NonEmptySeq[B]] =
    super.groupMap(key)(f).mapVals(_.assumedNonEmpty)

  final override def grouped(size: Int): Iterator[NonEmptySeq[A]] =
    super.grouped(size).map(_.assumedNonEmpty)
  final override def sliding(size: Int): Iterator[NonEmptySeq[A]] =
    super.sliding(size).map(_.assumedNonEmpty)
  final override def sliding(size: Int, step: Int): Iterator[NonEmptySeq[A]] =
    super.sliding(size, step).map(_.assumedNonEmpty)

  final override def permutations: Iterator[NonEmptySeq[A]] =
    super.permutations.map(_.assumedNonEmpty)
  final override def combinations(n: Int): Iterator[NonEmptySeq[A]] =
    super.combinations(n).map(_.assumedNonEmpty)

  override def iterableFactory: SeqFactory[ISeq] = ISeq
  override def fromSpecific(as: IterableOnce[A @uncheckedVariance]): ISeq[A] =
    iterableFactory.fromSpecific(as)
end ISeq

object ISeq extends StrictOptimizedSeqFactory[ISeq]:
  override def newBuilder[A]: ISeqBuilder[A] = new ISeqBuilder[A]
  override def empty[A]: ISeq[A] = EmptySeq
  override def from[A](source: IterableOnce[A]): ISeq[A] = source.asMatchable match
    case iSeq: ISeq[A @unchecked] => iSeq
    case _ => new ISeq(IndexedSeq.from(source))

final private class ISeqBuilder[A] extends Builder[A, ISeq[A]]:
  private val wrapped = IndexedSeq.newBuilder[A]
  override def addOne(elem: A): this.type = this.tap(_ => wrapped.addOne(elem))
  override def result(): ISeq[A] = ISeq.from(wrapped.result())
  override def clear(): Unit = wrapped.clear()

object EmptySeq extends ISeq[Nothing](IndexedSeq.empty):
  override val isEmpty: Boolean = true

extension[A](as: IterableOnce[A]) def indexed: ISeq[A] = ISeq.from(as)

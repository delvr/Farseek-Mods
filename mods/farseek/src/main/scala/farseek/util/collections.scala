package farseek.util

import farseek.util.imports.*

type Index = NonNegativeInt
val  Index = NonNegativeInt

extension[A](as: AnyIterable[A])
  def first: A = as.head // counterpart to `last`
  def singletonOption: Option[A] = when(as.size == 1)(first)
  def toMapWith[K, V](kv: A => (K, V)): Map[K, V] = as.map(kv).toMap
  def toMapWith[K, V](k: A => K, v: A => V): Map[K, V] = toMapWith(a => k(a) -> v(a))
  def mappedTo [V](f: A => V): Map[A, V] = toMapWith(identity, f)
  def keyedWith[K](f: A => K): Map[K, A] = toMapWith(f, identity)
  def allHaveSame(f: A => Any): Boolean = uniqueMappingFor(f).isDefined
  def uniqueMappingFor[B](f: A => B): Option[B] =
    as.headOption.map(f).filter(b => as.tail.forall(f(_) == b))

extension[A](as: AnySeq[A])
  def sumOption    [B >: A: Addition]: Option[B] = as.whenNonEmpty(_.sum)
  def productOption[B >: A: Multiplication]: Option[B] = as.whenNonEmpty(_.product)
  def averageOption[B >: A: Addition](using B ScaledBy `(0,1]`): Option[B] = as.whenNonEmpty(_.average)
  def firstIndexWhere(p: A => Boolean): Option[Index] = Index.option(as.indexWhere(p))
  def finalIndexWhere(p: A => Boolean): Option[Index] = Index.option(as.lastIndexWhere(p))
  def slicesWhere(p: A => Boolean): ISeq[NonEmptySeq[A]] =
    as.dropWhile(!p(_)).nonEmptyOption.fold(EmptySeq): sliceAndRest =>
      val (slice, rest) = sliceAndRest.span(p)
      slice.assumedNonEmpty +:: rest.slicesWhere(p)

extension[K, V](kvs: Map[K, V])
  def mapKeys[KK](f: K => KK): Map[KK, V] = kvs.map((k, v) => f(k) -> v)
  def mapVals[VV](f: V => VV): Map[K, VV] = kvs.map((k, v) => k -> f(v))
  def filterByKey(f: K => Boolean): Map[K, V] = kvs.filter((k, _) => f(k))
  def filterByVal(f: V => Boolean): Map[K, V] = kvs.filter((_, v) => f(v))
  def invert: Map[V, NonEmptySeq[K]] = kvs.groupMap(_._2)(_._1).mapVals(NonEmptySeq.from)

def mutablePriorityQueue[T: Ordering as ordering](xs: T*): PriorityQueue[T] =
  val queue = new PriorityQueue(ordering); queue.addAll(xs.asJava); queue

extension[T](queue: PriorityQueue[T]) def upsert(x: T): Unit =
  queue.remove(x); queue.add(x): Unit

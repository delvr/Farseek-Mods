package farseek.util

import farseek.util.imports.*

@sam trait Enumerable[T] extends ((T, T) => NonEmptySeq[T]):
  extension(start: T) infix def towards(end: T): NonEmptySeq[T] =
    if start == end then NonEmptySeq(start) else apply(start, end)

extension[T: {Enumerable, Ordering}](start: T)
  infix def forwardsTo (end: T): IndexedSeqView[T] =
    (if start <= end then start towards end else EmptySeq).view
  infix def backwardsTo(end: T): IndexedSeqView[T] =
    (if start >= end then start towards end else EmptySeq).view

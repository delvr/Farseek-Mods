package farseek.util

import farseek.util.imports.*

/** [[https://en.wikipedia.org/wiki/Set_membership Membership]] relation. */
@sam infix trait CanContain[-S, -E] extends ((S, E) => Boolean):
  extension(s: into[S]) infix def contains(e: into[E]): Boolean = apply(s, e)
  extension(e: into[E]) infix def memberOf(s: into[S]): Boolean = apply(s, e)

/** [[https://en.wikipedia.org/wiki/Subset Inclusion]] (subset/superset) relation. */
@sam infix trait CanInclude[-Sup, -Sub] extends ((Sup, Sub) => Boolean):
  extension(sup: into[Sup]) infix def includes(sub: into[Sub]): Boolean = apply(sup, sub)
  extension(sub: into[Sub]) infix def subsetOf(sup: into[Sup]): Boolean = apply(sup, sub)

@sam infix trait ContainerOf[-E]: // supertrait can have variance even if implementor doesn't
  infix def hasMember(e: E): Boolean // not "contains" as it can conflict with Iterable method

@sam infix trait SupersetOf[-S]: // supertrait can have variance even if implementor doesn't
  infix def includes(s: S): Boolean

extension[E](e: into[E]) // duplicating the `using` clause here is necessary to avoid ambiguity
  infix def isIn [S](s: into[S])(using S CanContain E): Boolean = s contains e
  def assumeIn   [S](s: into[S])(using S CanContain E): Unit = e.assert (_ isIn s, "not in " + s)
  def validateIn [S](s: into[S])(using S CanContain E): Unit = e.require(_ isIn s, "not in " + s)
  def assumedIn  [S](s: into[S])(using S CanContain E): E = e.tap(_.assumeIn(s))
  def validatedIn[S](s: into[S])(using S CanContain E): E = e.tap(_.validateIn(s))
  def someWhenIn [S](s: into[S])(using S CanContain E): Option[E] = e.someWhen(_ isIn s)
  def whenIn  [T, S](s: into[S])(using S CanContain E)(f: E => T): Option[T] = e.when(_ isIn s)(f)

extension[Sub](sub: Sub) // duplicating the `using` clause here is necessary to avoid ambiguity
  infix def isFullyIn [Sup](sup: into[Sup])(using Sup CanInclude Sub): Boolean = sup includes sub
  def assumeFullyIn   [Sup](sup: into[Sup])(using Sup CanInclude Sub): Unit = sub.assert (_ isFullyIn sup, "not (fully) in " + sup)
  def validateFullyIn [Sup](sup: into[Sup])(using Sup CanInclude Sub): Unit = sub.require(_ isFullyIn sup, "not (fully) in " + sup)
  def assumedFullyIn  [Sup](sup: into[Sup])(using Sup CanInclude Sub): Sub = sub.tap(_.assumeFullyIn(sup))
  def validatedFullyIn[Sup](sup: into[Sup])(using Sup CanInclude Sub): Sub = sub.tap(_.validateFullyIn(sup))
  def someWhenFullyIn [Sup](sup: into[Sup])(using Sup CanInclude Sub): Option[Sub] = sub.someWhen(_ isFullyIn sup)
  def whenFullyIn  [T, Sup](sup: into[Sup])(using Sup CanInclude Sub)(f: Sub => T): Option[T] = sub.when(_ isFullyIn sup)(f)

type Inclusion[S] = S CanInclude S

given containerMembership: [E] => ContainerOf[E] CanContain E = _ hasMember _
given supersetInclusion:   [S] =>  SupersetOf[S] CanInclude S = _ includes _

given setMembership: [S <: AnySet[E], E] => (S NotA ContainerOf[E]) => S CanContain E = _ contains _
given seqMembership: [S <: AnySeq[E], E] => (S NotA ContainerOf[E]) => S CanContain E = _ contains _

given setInclusion: [S <: AnySet[E], E] => (S NotA SupersetOf[S]) => Inclusion[S] = (sup, sub) => sub subsetOf sup
given seqInclusion: [S <: AnySeq[E], E] => (S NotA SupersetOf[S]) => Inclusion[S] = (sup, sub) => sub.forall(sup.contains)

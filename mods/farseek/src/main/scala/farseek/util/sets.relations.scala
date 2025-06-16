package farseek.util

import farseek.util.imports.*

/** [[https://en.wikipedia.org/wiki/Set_membership Membership]] relation. */
@sam infix trait CanContain[-S, -E] extends ((S, E) => Boolean):
  extension(s: into S) infix def contains(e: into E): Boolean = apply(s, e)
  extension(e: into E) infix def memberOf(s: into S): Boolean = apply(s, e)

/** [[https://en.wikipedia.org/wiki/Subset Inclusion]] (subset/superset) relation. */
@sam infix trait CanInclude[-Sup, -Sub] extends ((Sup, Sub) => Boolean):
  extension(sup: into Sup) infix def includes(sub: into Sub): Boolean = apply(sup, sub)
  extension(sub: into Sub) infix def subsetOf(sup: into Sup): Boolean = apply(sup, sub)

@sam infix trait ContainerOf[-E]:
  infix def hasMember(e: E): Boolean // not "contains" as it can conflict with Iterable method

@sam infix trait SupersetOf[-S]:
  infix def includes(s: S): Boolean

extension[E](e: into E) // duplicating the `using` clause here is necessary to avoid ambiguity
  infix def isIn [S](s: into S)(using S CanContain E): Boolean = s contains e
  def assumeIn   [S](s: into S)(using S CanContain E): Unit = e.assert (_ isIn s, s"not in ${s.toString}")
  def validateIn [S](s: into S)(using S CanContain E): Unit = e.require(_ isIn s, s"not in ${s.toString}")
  def assumedIn  [S](s: into S)(using S CanContain E): E = e.tap(_.assumeIn(s))
  def validatedIn[S](s: into S)(using S CanContain E): E = e.tap(_.validateIn(s))
  def someWhenIn [S](s: into S)(using S CanContain E): Option[E] = e.someWhen(_ isIn s)

extension[E, T](e: into E)
  def whenIn[S](s: into S)(f: E => T)(using S CanContain E): Option[T] = e.someWhenIn(s).map(f)

type Inclusion[S] = S CanInclude S

given [E] => ContainerOf[E] CanContain E = _ hasMember _
given [S] =>  SupersetOf[S] CanInclude S = _ includes _

given setMembership: [S <: AnySet[E], E] => (@unused S: S NotA ContainerOf[E])
  => S CanContain E = _ contains _
given seqMembership: [S <: AnySeq[E], E] => (@unused S: S NotA ContainerOf[E])
  => S CanContain E = _ contains _

given setInclusion: [S <: AnySet[E], E] => (@unused S: S NotA SupersetOf[S])
  => Inclusion[S] = (sup, sub) => sub subsetOf sup
given seqInclusion: [S <: AnySeq[E], E] => (@unused S: S NotA SupersetOf[S])
  => Inclusion[S] = (sup, sub) => sub.forall(sup.contains)

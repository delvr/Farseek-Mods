package farseek.util

/** A non-empty, closed, bounded [[https://en.wikipedia.org/wiki/Interval_(mathematics) interval]] of [[P]]. */
sealed trait Interval[P: Ordering] extends ContainerOf[P], SupersetOf[Interval[P]]:
  def min: P
  def max: P
  private[util] def validated: this.type = this.required(_ => min <= max, "non-empty")
  final override infix def hasMember(p: P): Boolean = p.isBetweenInclusive(min, max)
  final override infix def includes (i: Interval[P]): Boolean = hasMember(i.min) && hasMember(i.max)
  final def clamp(p: P): P = p.clampBetween(min, max)
  final def conversion: MinMaxConversion[P, P] = MinMaxConversion(this, identity)
  final override def toString: String = s"[${min.toString}$separator${max.toString}]"
  protected def separator: String

final case class ContinuousInterval[P: {Ordering, Continuous}] private(min: P, max: P)
    extends DirectedLineSegment[P](min, max), Interval[P]:
  override protected def separator: String = ", "

final case class DiscreteInterval[P: {Ordering, Discrete}] private(min: P, max: P)
    extends NonEmptySeq[P](min towards max), Interval[P]:
  override protected def separator: String = " .. "

object ContinuousInterval:
  def apply[P: {Ordering, Continuous}](min: P, max: P): ContinuousInterval[P] =
    new ContinuousInterval(min, max).validated

object DiscreteInterval:
  def apply[P: {Ordering, Discrete}](min: P, max: P): DiscreteInterval[P] =
    new DiscreteInterval(min, max).validated

extension[P: Ordering](p: P)(using P: ContinuousOrDiscrete[P])
  transparent inline def interval : Interval[P] = p <-> p
  transparent inline def <->(q: P): Interval[P] = inline P match
    case given Continuous[P] => ContinuousInterval(p, q)
    case given   Discrete[P] =>   DiscreteInterval(p, q)

extension[P: {Ordering, ContinuousOrDiscrete}](i: Interval[P])
  transparent inline def expandBy[D](d: D)(using P ShiftedBy D, Negation[D]): Interval[P] =
    i.min :- d <-> i.max :+ d
  transparent inline def contractBy[D](d: D)(using P ShiftedBy D, Negation[D]): Interval[P] =
    expandBy(-d)
  transparent inline infix def span(j: Interval[P]): Interval[P] =
    minOf(i.min, j.min) <-> maxOf(i.max, j.max)

extension[P: {Ordering, Discrete, IntShift}](p: P)
  def extendToCount(n: PositiveInt): DiscreteInterval[P] = p <-> p :+ (n - 1)

extension[P: {Ordering, Continuous, RealShift}](p: P)
  def extendToLength(d: NonNegativeReal): ContinuousInterval[P] = p <-> p :+ d

extension[P](p: P)
  def clampedIn(bounds: Interval[P]): P = bounds.clamp(p)
//  def clamped(using bounds: Interval[P]): P = clampedIn(bounds)
//  def someWhenInBounds(using bounds: Interval[P]): Option[P] = p.someWhenIn(bounds)

extension[P, D](p: P)(using P SeparatedBy D)
  infix def within(bounds: Interval[P]): D = p.validatedIn(bounds) relativeTo bounds.min

given continuousIntervalMap: [A, B: {Ordering, Continuous}] => (A Into B)
    => ContinuousInterval[A] Into ContinuousInterval[B] = i => i.min.into[B] <-> i.max.into[B]

given discreteIntervalMap: [A, B: {Ordering, Discrete}] => (A Into B)
    => DiscreteInterval[A] Into DiscreteInterval[B] = i => i.min.into[B] <-> i.max.into[B]

given continuousIntervalFlatMap: [A, B: {Ordering, Continuous}] => (A Into ContinuousInterval[B])
    => ContinuousInterval[A] Into ContinuousInterval[B] =
  i => i.min.into[ContinuousInterval[B]].min <-> i.max.into[ContinuousInterval[B]].max

given discreteIntervalFlatMap: [A, B: {Ordering, Discrete}] => (A Into DiscreteInterval[B])
    => DiscreteInterval[A] Into DiscreteInterval[B] =
  i => i.min.into[DiscreteInterval[B]].min <-> i.max.into[DiscreteInterval[B]].max

package farseek.util

type Continuous[P] = Interpolation[P]
type Discrete  [P] =    Enumerable[P]
type ContinuousOrDiscrete[P] = Continuous[P] | Discrete[P]

/** A non-empty, closed, bounded [[https://en.wikipedia.org/wiki/Interval_(mathematics) interval]] of [[P]]. */
sealed trait Interval[P: Ordering] extends ContainerOf[P], SupersetOf[Interval[P]]:
  def min: P
  def max: P
  private[util] def validated: this.type = this.required(_ => min <= max, "non-empty")
  final override infix def hasMember(p: P): Boolean = p.isBetweenInclusive(min, max)
  final override infix def includes(i: Interval[P]): Boolean = hasMember(i.min) && hasMember(i.max)
  final def clamp(p: P): P = p.clampBetween(min, max)
  protected def separator: String
  final override def toString: String = s"[${min.toString}$separator${max.toString}]"
  /** [[https://en.wikipedia.org/wiki/Bounded_quantification F-bounded type]] */
  type I <: Interval[P]
  def copyWith(min: P, max: P): I
  final def extendBy  [D](d: D)(using P ShiftedBy D): I = copyWith(min, max :+ d)
  final def expandBy  [D](d: D)(using P ShiftedBy D, Negation[D]): I = copyWith(min :- d, max :+ d)
  final def contractBy[D](d: D)(using P ShiftedBy D, Negation[D]): I = expandBy(-d)
  final infix def span(that: Interval[P]): I =
    copyWith(minOf(this.min, that.min), maxOf(this.max, that.max))

final case class ContinuousInterval[P: {Ordering, Continuous}] private(min: P, max: P)
    extends DirectedLineSegment[P](min, max), Interval[P]:
  override type I = ContinuousInterval[P]
  override def copyWith(min: P, max: P): I = copy(min, max)
  override protected def separator: String = ", "

final case class DiscreteInterval[P: {Ordering, Discrete}] private(min: P, max: P)
    extends NonEmptySeq[P](min towards max), Interval[P]:
  override type I = DiscreteInterval[P]
  override def copyWith(min: P, max: P): I = copy(min, max)
  override protected def separator: String = " .. "

object ContinuousInterval:
  def apply[P: {Ordering, Continuous}](min: P, max: P): ContinuousInterval[P] =
    new ContinuousInterval(min, max).validated

object DiscreteInterval:
  def apply[P: {Ordering, Discrete}](min: P, max: P): DiscreteInterval[P] =
    new DiscreteInterval(min, max).validated

extension[P: Ordering](p: P)(using P: ContinuousOrDiscrete[P])
  transparent inline def <->(q: P): Interval[P] = inline P match
    case given Continuous[P] => ContinuousInterval(p, q)
    case given   Discrete[P] =>   DiscreteInterval(p, q)
  transparent inline def singleton: Interval[P] = p <-> p
  transparent inline def interval : Interval[P] = singleton

extension[P: {Ordering, Discrete, IntShift}](p: P)
  def extendToCount(n: PositiveInt): DiscreteInterval[P] = p <-> p :+ (n - 1)

extension[P: {Ordering, Continuous, RealShift}](p: P)
  def extendToLength(d: NonNegativeReal): ContinuousInterval[P] = p <-> p :+ d

extension[P](p: P)
  def clampIn(bounds: Interval[P]): P = bounds.clamp(p)
  def clamped(using bounds: Interval[P]): P = bounds.clamp(p)
  def someWhenInBounds(using bounds: Interval[P]): Option[P] = p.someWhenIn(bounds)

given continuousIntervalShift: [P: {Ordering, Continuous}, D] => (P ShiftedBy D)
    => ContinuousInterval[P] ShiftedBy D = (i, d) => (i.min :+ d) <-> (i.max :+ d)

given discreteIntervalShift: [P: {Ordering, Discrete}, D] => (P ShiftedBy D)
    => DiscreteInterval[P] ShiftedBy D = (i, d) => (i.min :+ d) <-> (i.max :+ d)

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

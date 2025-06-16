package farseek.util

import farseek.util.imports.*

private val MinReal = Int.MinValue.toDouble
private val MaxReal = Int.MaxValue.toDouble

// Refinements
// ------------------------------------------------------------------------------------------------
type NonZeroDouble = NonZero[Double]
val NonZeroDouble: Double IntoOption NonZeroDouble = nonZero

opaque type Real <: Double = Double
val Real: Double IntoOption Real = MinMaxConversion(MinReal, MaxReal, identity)

type NonZeroReal = NonZero[Real]
val NonZeroReal: Double IntoOption NonZeroReal = Real andThen NonZeroDouble

opaque type NonNegativeReal <: Real = Double
val NonNegativeReal: Double IntoOptionWithMin NonNegativeReal = MinConversion(0.0, identity)

type PositiveReal = NonZero[NonNegativeReal]
val PositiveReal: Double IntoOption PositiveReal = NonNegativeReal andThen NonZeroReal

opaque type `[-1,1]` <: Real = Double
val `[-1,1]`: Double IntoOptionWithMinMax `[-1,1]` = MinMaxConversion(-1.0, 1.0, identity)

type `[0,1]` = `[-1,1]` & NonNegativeReal
val `[0,1]`: Double IntoOptionWithMinMax `[0,1]` = MinMaxConversion(0.0, 1.0, identity)

type `(0,1]` = NonZero[`[0,1]`]
val `(0,1]`: Double IntoOption `(0,1]` = `[0,1]` andThen NonZeroReal

// Constants
// ------------------------------------------------------------------------------------------------
val `0.0` = `[0,1]`( 0.0)
val `0.5` = `(0,1]`( 0.5)
val `1.0` = `(0,1]`( 1.0)

// Operations
// ------------------------------------------------------------------------------------------------
type RealShift[T] = T ShiftedBy Real
type RealDelta[T] = T SeparatedBy Real

given RealNegation: Negation[Real] = a => Real(-a)
given RealAddition: Addition[Real] = (a, b) => Real(a + b)
given RealSubtraction: Subtraction[Real] = (a, b) => Real(a - b)
given RealMultiplication: Multiplication[Real] = (a, b) => Real(a * b)
given RealInterpolation :  Interpolation[Real] = (t, p, q) => Real(fma(t, q - p, p))

val RealOrdering: Ordering[Real] = summon

extension[V](v: V)(using V ScaledBy NonZeroReal)
  def :/(s: into NonZeroReal): V = v :* NonZeroReal(1/s)

/** @see [[java.math.RoundingMode]] */
@sam trait Rounding extends (Real => Int)
object Rounding:
  /** [[java.math.RoundingMode.FLOOR]] */
  val Floor     : Rounding = _.floor.toInt
  /** [[java.math.RoundingMode.CEILING]] */
  val Ceiling   : Rounding = _.ceil.toInt
  /** [[java.math.RoundingMode.HALF_UP]] */
  val TiesToAway: Rounding = _.round.toInt

// Geometry
// ------------------------------------------------------------------------------------------------
type Real2 = (Real, Real)
type Real3 = (Real, Real, Real)

type NonZeroReal2 = NonZero[Real2]
type NonZeroReal3 = NonZero[Real3]

object Real2:
  val Zero = (`0.0`, `0.0`)
object Real3:
  val Zero = (`0.0`, `0.0`, `0.0`)

extension(ov: Option[Real2]) def orZero: Real2 = ov.getOrElse(Real2.Zero)
extension(ov: Option[Real3]) def orZero: Real3 = ov.getOrElse(Real3.Zero)

// Trigonometry
// ------------------------------------------------------------------------------------------------
opaque type Degrees = Double
opaque type Radians = Double

given Radians Into Degrees = Math.toDegrees
given Degrees Into Radians = Math.toRadians

val Pi  = PositiveReal(Math.PI)
val Tau = PositiveReal(Math.TAU)

def sin(x: Radians): `[-1,1]` = `[-1,1]`(Math.sin(x))
def cos(x: Radians): `[-1,1]` = `[-1,1]`(Math.cos(x))

def arcsin(x: `[-1,1]`): Radians = Math.asin(x)
def arccos(x: `[-1,1]`): Radians = Math.acos(x)

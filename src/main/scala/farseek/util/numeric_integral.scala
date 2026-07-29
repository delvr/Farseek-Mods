package farseek.util

import farseek.util.imports.*

// Refinements
// ------------------------------------------------------------------------------------------------
type NonZeroInt = NonZero[Int]
val NonZeroInt: Int IntoOption NonZeroInt = nonZero

opaque type NonNegativeInt <: Int = Int
val NonNegativeInt: Int IntoOptionWithMin NonNegativeInt = MinConversion(0, identity)

type PositiveInt = NonZero[NonNegativeInt]
val PositiveInt: Int IntoOptionWithMin PositiveInt = MinConversion(1, NonNegativeInt andThen NonZeroInt)

// Constants
// ------------------------------------------------------------------------------------------------
val `0` = NonNegativeInt(0)
val `1` = PositiveInt( 1)
val `2` = PositiveInt( 2)
val `3` = PositiveInt( 3)
val `4` = PositiveInt( 4)
val `8` = PositiveInt( 8)
val `10` = PositiveInt(10)
val `16` = PositiveInt(16)
val `32` = PositiveInt(32)
val `64` = PositiveInt(64)

// Operations
// ------------------------------------------------------------------------------------------------
type IntShift[T] = T ShiftedBy Int
type IntDelta[T] = T SeparatedBy Int

given IntNegation: Negation[Int] = negateExact
given IntAddition: Addition[Int] = addExact
given IntSubtraction: Subtraction[Int] = subtractExact
given IntMultiplication: Multiplication[Int] = multiplyExact
given IntModulo: Modulo[Int] = _ % _
given IntEnumeration: SeqEnumeration[Int] = (start, end) =>
  Range.inclusive(start, end, step = if start <= end then +1 else -1).assumedNonEmpty

val IntOrdering: Ordering[Int] = summon

@sam infix trait ToInt[-A, +B <: Int] extends (A => B): // NOT an implicit conversion
  extension(a: A) def toInt: B = apply(a)

extension(n: NonNegativeInt) def plusOne :    PositiveInt =    PositiveInt(n + 1)
extension(n:    PositiveInt) def minusOne: NonNegativeInt = NonNegativeInt(n - 1)

def zeroTo(n: PositiveInt): NonEmptySeq[NonNegativeInt] =
  (`0` to n).map(NonNegativeInt).assumedNonEmpty
def oneTo(n: PositiveInt): NonEmptySeq[PositiveInt] =
  (`1` to n).map(PositiveInt).assumedNonEmpty

given intRoundedScaling: (Rounding) => Int ScaledBy Real = (x, s) => Real(x * s).rounded

/** [[https://en.wikipedia.org/wiki/Modulo modulo]] */
@sam trait Modulo[-A] extends ((A, NonZeroInt) => Int):
  extension(a: A)
    def %(n: PositiveInt): Int = apply(a, n)
    infix def isMultipleOf(n: PositiveInt): Boolean = a % n == 0
    def isEven: Boolean = isMultipleOf(`2`)
    def isOdd : Boolean = !isEven

// Flags
// ------------------------------------------------------------------------------------------------
opaque type Flags = NonNegativeInt
val NoFlags: Flags = 0
object Flags:
  def apply(flags: PositiveInt*): Flags = flags.fold(NoFlags)(_ | _)
  given Flags ToInt NonNegativeInt = identity

given ZeroTest [Flags] = _ == NoFlags
given EmptyTest[Flags] = _ == NoFlags

given Union       [Flags] = (f1, f2) => f1 | f2
given Intersection[Flags] = (f1, f2) => f1 & f2
given Inclusion   [Flags] = (sup, sub) => (sub & sup) == sub

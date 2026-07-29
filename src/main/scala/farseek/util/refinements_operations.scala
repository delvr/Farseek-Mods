package farseek.util

given Addition[NonNegativeInt ] = NonNegativeInt.op(IntAddition)
given Addition[NonNegativeReal] = NonNegativeReal.op(RealAddition)

given Addition[PositiveInt ] = PositiveInt.op(IntAddition)
given Addition[PositiveReal] = PositiveReal.op(RealAddition)

given Multiplication[NonNegativeInt ] = NonNegativeInt.op(IntMultiplication)
given Multiplication[NonNegativeReal] = NonNegativeReal.op(RealMultiplication)

given Multiplication[PositiveInt ] = PositiveInt.op(IntMultiplication)
given Multiplication[PositiveReal] = PositiveReal.op(RealMultiplication)

given Multiplication[`[-1,1]`] = `[-1,1]`.op(RealMultiplication)
given Multiplication[ `[0,1]`] =  `[0,1]`.op(RealMultiplication)

given Interpolation[NonNegativeReal] = NonNegativeReal.op(RealInterpolation)
given Interpolation[   PositiveReal] = PositiveReal.op(RealInterpolation)

given Interpolation[`[-1,1]`] = `[-1,1]`.op(RealInterpolation)
given Interpolation[ `[0,1]`] =  `[0,1]`.op(RealInterpolation)
given Interpolation[ `(0,1]`] =  `(0,1]`.op(RealInterpolation)

given SeqEnumeration[NonNegativeInt] = IntEnumeration(_, _).map(NonNegativeInt)
given SeqEnumeration[   PositiveInt] = IntEnumeration(_, _).map(PositiveInt)

given (Rounding) => NonNegativeInt ScaledBy NonNegativeReal =
  (x, s) => NonNegativeInt(intRoundedScaling(x, s))

extension(x: Int)
  def clampZeroTo(max: NonNegativeInt): NonNegativeInt = NonNegativeInt.clamp(x, max)
  def clampOneTo (max: PositiveInt):    PositiveInt =       PositiveInt.clamp(x, max)
  transparent inline def toReal: Real = inline x match
    case _: PositiveInt    =>    PositiveReal(x.toDouble)
    case _: NonNegativeInt => NonNegativeReal(x.toDouble)
    case _: NonZeroInt     =>     NonZeroReal(x.toDouble)
    case _                 =>            Real(x.toDouble)

extension(x: Real)
  def clampZeroTo(max: NonNegativeReal): NonNegativeReal = NonNegativeReal.clamp(x, max)
  transparent inline def rounded(using round: Rounding = Rounding.TiesToAway): Int = inline x match
    case _: NonNegativeReal => NonNegativeInt(round(x)) // includes PositiveReal ex.: 0.1
    case _                  =>                round(x)  // includes NonZeroReal ex.: -0.1

transparent inline def fraction(n: Int, m: PositiveInt): Real = inline n match
  case _:    PositiveInt => `(0,1]`(n.toDouble / m.toDouble)
  case _: NonNegativeInt => `[0,1]`(n.toDouble / m.toDouble)
  case _                 =>   Real (n.toDouble / m.toDouble)

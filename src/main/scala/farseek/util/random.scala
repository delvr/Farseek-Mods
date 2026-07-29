package farseek.util

import farseek.util.imports.*

type Probability = `[0,1]`
val  Probability = `[0,1]`

val Never:      Probability = `0.0`
val HalfChance: Probability = `0.5`
val Always:     Probability = `1.0`

trait RandomGenerator[R]:
  protected def nextDouble(random: R): Double
  protected def nextInt(random: R, bound: Int): Int
  extension(random: R)
    def nextFraction(): `[0,1]` = `[0,1]`(nextDouble(random))
    def nextIntBelow(n: PositiveInt): NonNegativeInt = NonNegativeInt(nextInt(random, n))
    def between(min: Int, max: Int): Int = min + zeroTo(NonNegativeInt(max - min))
    def between[T: Interpolation](min: T, max: T): T = nextFraction().lerp(min, max)
    def zeroTo(n: NonNegativeInt): NonNegativeInt = nextIntBelow(n.plusOne)
    def oneTo(n: PositiveInt): PositiveInt = nextIntBelow(n).plusOne
    def among(n: PositiveInt): Boolean = nextIntBelow(n) == 0
    def indexOf  [E](es: NonEmptySeq[E]): Index = zeroTo(es.lastIndex)
    def elementOf[E](es: NonEmptySeq[E]): E = es(indexOf(es))

given JavaRandom: RandomGenerator[JRandom]:
  override protected def nextDouble(random: JRandom): Double = random.nextDouble()
  override protected def nextInt(random: JRandom, bound: Int): Int = random.nextInt(bound)

given ScalaRandom: RandomGenerator[Random]:
  override protected def nextDouble(random: Random): Double = random.nextDouble()
  override protected def nextInt(random: Random, bound: Int): Int = random.nextInt(bound)

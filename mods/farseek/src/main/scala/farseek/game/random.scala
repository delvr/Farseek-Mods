package farseek.game

import farseek.game.imports.*
import farseek.util.*

given RandomGenerator[RandomSource]:
  override protected def nextDouble(random: RandomSource): Double = random.nextDouble()
  override protected def nextInt(random: RandomSource, bound: Int): Int = random.nextInt(bound)

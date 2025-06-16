package streams.world.gen

import farseek.game.*
import farseek.util.{*, given}

opaque type BasinX = Int
opaque type BasinZ = Int

object BasinX:
  given Ordering  [BasinX] = IntOrdering
  given IntShift  [BasinX] = IntAddition(_, _)
  given IntDelta  [BasinX] = IntSubtraction(_, _)
  given Modulo    [BasinX] = IntModulo
  given Enumerable[BasinX] = IntEnumeration

object BasinZ:
  given Ordering  [BasinZ] = IntOrdering
  given IntShift  [BasinZ] = IntAddition(_, _)
  given IntDelta  [BasinZ] = IntSubtraction(_, _)
  given Modulo    [BasinZ] = IntModulo
  given Enumerable[BasinZ] = IntEnumeration

type BasinXZ = (BasinX, BasinZ)
type BasinXZBox = XZGridBox[BasinX, BasinZ]

private val BasinScaleBits = `3`
private val BasinChunkSize = PositiveInt(1 << BasinScaleBits)

given ChunkX Into BasinX = ChunkX.toInt(_) >> BasinScaleBits
given ChunkZ Into BasinZ = ChunkZ.toInt(_) >> BasinScaleBits

given BasinX Into GridBox[ChunkX] = x => ChunkX(x << BasinScaleBits).extendToCount(BasinChunkSize)
given BasinZ Into GridBox[ChunkZ] = z => ChunkZ(z << BasinScaleBits).extendToCount(BasinChunkSize)

// minimum 2, assuming each main-stem basin can have either a North OR a South tributary
val MainBasinZSpacing = `4`

extension(p: into BasinXZ) // meander direction, flowing West
  def bendsLeft: Boolean = p.x.isEven == p.z.isEven
  def bendsRight: Boolean = !bendsLeft

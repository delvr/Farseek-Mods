package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}

opaque type ChunkX = Int
opaque type ChunkZ = Int

object ChunkX:
  def apply(x: Int): ChunkX = x
  def toInt(x: ChunkX): Int = x
  given Ordering[ChunkX] = IntOrdering
  given IntShift[ChunkX] = IntAddition(_, _)
  given IntDelta[ChunkX] = IntSubtraction(_, _)
  given Modulo  [ChunkX] = IntModulo
  given Enumerable[ChunkX] = IntEnumeration

object ChunkZ:
  def apply(z: Int): ChunkZ = z
  def toInt(z: ChunkZ): Int = z
  given Ordering[ChunkZ] = IntOrdering
  given IntShift[ChunkZ] = IntAddition(_, _)
  given IntDelta[ChunkZ] = IntSubtraction(_, _)
  given Modulo  [ChunkZ] = IntModulo
  given Enumerable[ChunkZ] = IntEnumeration

given ChunkPos Into ChunkXZ = p => (p.x, p.z)
given ChunkXZ Into ChunkPos = p => new ChunkPos(p.x, p.z)

type ChunkXZ = (ChunkX, ChunkZ)
type ChunkXZBox = XZGridBox[ChunkX, ChunkZ]

given BlockX Into ChunkX = x => blockToSectionCoord(BlockX.toInt(x))
given BlockZ Into ChunkZ = z => blockToSectionCoord(BlockZ.toInt(z))

val ChunkWidth = `16`

extension(x: ChunkX) def minBlockX: BlockX = BlockX(sectionToBlockCoord(x))
extension(z: ChunkZ) def minBlockZ: BlockZ = BlockZ(sectionToBlockCoord(z))
extension(p: ChunkXZ) def minBlockXZ: BlockXZ = (p.x.minBlockX, p.z.minBlockZ)

given ChunkX Into GridBox[BlockX] = _.minBlockX.extendToCount(ChunkWidth)
given ChunkZ Into GridBox[BlockZ] = _.minBlockZ.extendToCount(ChunkWidth)

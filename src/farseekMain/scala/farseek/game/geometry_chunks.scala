package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.core.SectionPos.sectionToBlockCoord

opaque type ChunkX = Int
opaque type ChunkZ = Int

object ChunkX:
  def apply(x: Int): ChunkX = x
  given ChunkX ToInt Int = identity
  given Ordering[ChunkX] = IntOrdering
  given IntShift[ChunkX] = IntAddition(_, _)
  given IntDelta[ChunkX] = IntSubtraction(_, _)
  given Modulo  [ChunkX] = IntModulo
  given SeqEnumeration[ChunkX] = IntEnumeration

object ChunkZ:
  def apply(z: Int): ChunkZ = z
  given ChunkZ ToInt Int = identity
  given Ordering[ChunkZ] = IntOrdering
  given IntShift[ChunkZ] = IntAddition(_, _)
  given IntDelta[ChunkZ] = IntSubtraction(_, _)
  given Modulo  [ChunkZ] = IntModulo
  given SeqEnumeration[ChunkZ] = IntEnumeration

given ChunkPos Into ChunkXZ = p => (p.x, p.z)
given ChunkXZ Into ChunkPos = p => new ChunkPos(p.x, p.z)

type ChunkXZ = (ChunkX, ChunkZ)
type ChunkXZBox = XZGridBox[ChunkX, ChunkZ]

extension(p: into[ChunkXZ])
  def chunkXZ: ChunkXZ = p

val ChunkWidth = `16`

extension(x: ChunkX ) def minBlockX : BlockX = BlockX(sectionToBlockCoord(x))
extension(z: ChunkZ ) def minBlockZ : BlockZ = BlockZ(sectionToBlockCoord(z))
extension(p: ChunkXZ) def minBlockXZ: BlockXZ = (p.x.minBlockX, p.z.minBlockZ)

given ChunkX Into GridBox[BlockX] = _.minBlockX.extendToCount(ChunkWidth)
given ChunkZ Into GridBox[BlockZ] = _.minBlockZ.extendToCount(ChunkWidth)

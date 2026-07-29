package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.core.SectionPos.blockToSectionCoord
import net.minecraft.world.level.levelgen.structure.*

opaque type BlockX = Int
opaque type BlockY = Int
opaque type BlockZ = Int

object BlockX:
  def apply(x: Int): BlockX = x
  given BlockX ToInt Int = identity
  given Ordering[BlockX] = IntOrdering
  given IntShift[BlockX] = IntAddition(_, _)
  given IntDelta[BlockX] = IntSubtraction(_, _)
  given Modulo  [BlockX] = IntModulo
  given SeqEnumeration[BlockX] = IntEnumeration
  val Interpolation: Interpolation[BlockX] = lerpBlocks

object BlockY:
  def apply(y: Int): BlockY = y
  given BlockY ToInt Int = identity
  given Ordering[BlockY] = IntOrdering
  given IntShift[BlockY] = IntAddition(_, _)
  given IntDelta[BlockY] = IntSubtraction(_, _)
  given Modulo  [BlockY] = IntModulo
  given SeqEnumeration[BlockY] = IntEnumeration
  val Interpolation: Interpolation[BlockY] = lerpBlocks

object BlockZ:
  def apply(z: Int): BlockZ = z
  given BlockZ ToInt Int = identity
  given Ordering[BlockZ] = IntOrdering
  given IntShift[BlockZ] = IntAddition(_, _)
  given IntDelta[BlockZ] = IntSubtraction(_, _)
  given Modulo  [BlockZ] = IntModulo
  given SeqEnumeration[BlockZ] = IntEnumeration
  val Interpolation: Interpolation[BlockZ] = lerpBlocks

given BlockPos Into BlockXZ  = p => (p.getX, p.getZ)
given BlockPos Into BlockXYZ = p => (p.getX, p.getY, p.getZ)
given BlockXYZ Into BlockPos = new BlockPos(_, _, _)

given BlockX Into ChunkX = x => ChunkX(blockToSectionCoord(x.toInt))
given BlockZ Into ChunkZ = z => ChunkZ(blockToSectionCoord(z.toInt))

given BlockPos Into ChunkXZ = _.into[BlockXZ].into[ChunkXZ]
given BlockXYZ Into ChunkXZ = _.into[BlockXZ].into[ChunkXZ]

given BlockX Into FreeBox[EuclidX] = x => EuclidX(x.toReal).extendToLength(`1.0`)
given BlockY Into FreeBox[EuclidY] = y => EuclidY(y.toReal).extendToLength(`1.0`)
given BlockZ Into FreeBox[EuclidZ] = z => EuclidZ(z.toReal).extendToLength(`1.0`)

given EuclidX Into BlockX = _.converted.floor.toInt
given EuclidY Into BlockY = _.converted.floor.toInt
given EuclidZ Into BlockZ = _.converted.floor.toInt

type BlockXZ = (BlockX, BlockZ)
type BlockXYZ = (BlockX, BlockY, BlockZ)

type BlockYBox = YGridBox[BlockY]
type BlockXZBox = XZGridBox[BlockX, BlockZ]
type BlockXYZBox = XYZGridBox[BlockX, BlockY, BlockZ]

given StructureBoundingBox Into BlockXYZBox =
  b => (b.minX <-> b.maxX, b.minY <-> b.maxY, b.minZ <-> b.maxZ)
given BlockXYZBox Into StructureBoundingBox =
  b => BoundingBox(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)

def lerpBlocks(t: `[0,1]`, p: Int, q: Int): Int =
  RealInterpolation(t, Real(p + 0.5), Real(q + 0.5)).floor.toInt

extension(p: into[BlockXZ])  def blockXZ: BlockXZ = p
extension(p: into[BlockXYZ]) def blockXYZ: BlockXYZ = p

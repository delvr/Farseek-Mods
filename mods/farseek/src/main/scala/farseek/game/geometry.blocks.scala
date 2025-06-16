package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.levelgen.structure.*

opaque type BlockX = Int
opaque type BlockY = Int
opaque type BlockZ = Int

object BlockX:
  def apply(x: Int): BlockX = x
  def toInt(x: BlockX): Int = x
  given Ordering[BlockX] = IntOrdering
  given IntShift[BlockX] = IntAddition(_, _)
  given IntDelta[BlockX] = IntSubtraction(_, _)
  given Modulo  [BlockX] = IntModulo
  given Enumerable[BlockX] = IntEnumeration
  val Interpolation: Interpolation[BlockX] = lerpBlocks

object BlockY:
  def apply(y: Int): BlockY = y
  def toInt(y: BlockY): Int = y
  given Ordering[BlockY] = IntOrdering
  given IntShift[BlockY] = IntAddition(_, _)
  given IntDelta[BlockY] = IntSubtraction(_, _)
  given Modulo  [BlockY] = IntModulo
  given Enumerable[BlockY] = IntEnumeration
  val Interpolation: Interpolation[BlockY] = lerpBlocks

object BlockZ:
  def apply(z: Int): BlockZ = z
  def toInt(z: BlockZ): Int = z
  given Ordering[BlockZ] = IntOrdering
  given IntShift[BlockZ] = IntAddition(_, _)
  given IntDelta[BlockZ] = IntSubtraction(_, _)
  given Modulo  [BlockZ] = IntModulo
  given Enumerable[BlockZ] = IntEnumeration
  val Interpolation: Interpolation[BlockZ] = lerpBlocks

extension(p: BlockPos)
  def xyz: BlockXYZ = (p.getX, p.getY, p.getZ)

given BlockPos Into BlockXYZ = _.xyz
given BlockPos Into BlockXZ  = _.xyz.xz
given BlockXYZ Into BlockXZ  = _.xz
given BlockXYZ Into BlockPos = _.usingXYZ(new BlockPos(_, _, _))

given BlockX Into FreeBox[EuclidX] = x => EuclidX(x.toReal).interval.extendBy(`1.0`)
given BlockY Into FreeBox[EuclidY] = y => EuclidY(y.toReal).interval.extendBy(`1.0`)
given BlockZ Into FreeBox[EuclidZ] = z => EuclidZ(z.toReal).interval.extendBy(`1.0`)

given EuclidX Into BlockX = EuclidX.toReal(_).floor.toInt
given EuclidY Into BlockY = EuclidY.toReal(_).floor.toInt
given EuclidZ Into BlockZ = EuclidZ.toReal(_).floor.toInt

extension(p: into BlockXZ)
  def usingXZ[T](f: (Int, Int) => T) = f(p.x, p.z)

extension(p: into BlockXYZ)
  def usingXYZ[T](f: (Int, Int, Int) => T) = f(p.x, p.y, p.z)

type BlockXZ = (BlockX, BlockZ)
type BlockXYZ = (BlockX, BlockY, BlockZ)

type BlockYBox = GridBox[BlockY]
type BlockXZBox = XZGridBox[BlockX, BlockZ]
type BlockXYZBox = XYZGridBox[BlockX, BlockY, BlockZ]

given StructureBoundingBox Into BlockXYZBox =
  b => (b.minX <-> b.maxX, b.minY <-> b.maxY, b.minZ <-> b.maxZ)
given BlockXYZBox Into StructureBoundingBox =
  b => BoundingBox(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)

def lerpBlocks(t: `[0,1]`, p: Int, q: Int): Int =
  RealInterpolation(t, Real(p + 0.5), Real(q + 0.5)).floor.toInt

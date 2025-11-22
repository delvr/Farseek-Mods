package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}

opaque type EuclidX = Real
opaque type EuclidY = Real
opaque type EuclidZ = Real

object EuclidX:
  def apply(x: Real): EuclidX = x
  given EuclidX Into Real = identity
  given Ordering[EuclidX] = RealOrdering
  given RealShift[EuclidX] = RealAddition(_, _)
  given RealDelta[EuclidX] = RealSubtraction(_, _)
  given Interpolation[EuclidX] = RealInterpolation

object EuclidY:
  def apply(y: Real): EuclidY = y
  given EuclidY Into Real = identity
  given Ordering[EuclidY] = RealOrdering
  given RealShift[EuclidY] = RealAddition(_, _)
  given RealDelta[EuclidY] = RealSubtraction(_, _)
  given Interpolation[EuclidY] = RealInterpolation

object EuclidZ:
  def apply(z: Real): EuclidZ = z
  given EuclidZ Into Real = identity
  given Ordering[EuclidZ] = RealOrdering
  given RealShift[EuclidZ] = RealAddition(_, _)
  given RealDelta[EuclidZ] = RealSubtraction(_, _)
  given Interpolation[EuclidZ] = RealInterpolation

type EuclidXZ = (EuclidX, EuclidZ)
type EuclidXYZ = (EuclidX, EuclidY, EuclidZ)

type EuclidXZBox = XZFreeBox[EuclidX, EuclidZ]
type EuclidXYZBox = XYZFreeBox[EuclidX, EuclidY, EuclidZ]

type XZVector  = Real2
type XYZVector = Real3

val XZVector  = Real2
val XYZVector = Real3

type NonZeroXZVector  = NonZeroReal2
type NonZeroXYZVector = NonZeroReal3

type XZVectorBox = XZFreeBox[Real, Real]
type XYZVectorBox = XYZFreeBox[Real, Real, Real]

extension(v: Vec3)
  def assumedAbsolute: EuclidXYZ = (Real(v.x), Real(v.y), Real(v.z))
  def assumedRelative: XYZVector = (Real(v.x), Real(v.y), Real(v.z))

extension(b: AABB)
  def assumedAbsolute: EuclidXYZBox =
    (Real(b.minX) <-> Real(b.maxX), Real(b.minY) <-> Real(b.maxY), Real(b.minZ) <-> Real(b.maxZ))
  def assumedRelative: XYZVectorBox =
    (Real(b.minX) <-> Real(b.maxX), Real(b.minY) <-> Real(b.maxY), Real(b.minZ) <-> Real(b.maxZ))

// No EuclidXZ Into Vec3 (affine space without zero Y)
given XZVector  Into Vec3 = v => new Vec3(v.x,   0, v.z)
given EuclidXYZ Into Vec3 = v => new Vec3(v.x, v.y, v.z)
given XYZVector Into Vec3 = v => new Vec3(v.x, v.y, v.z)

given XYZVectorBox Into AABB = b => new AABB(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)
given EuclidXYZBox Into AABB = b => new AABB(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)

extension(v: XZVector)
  def x0z: XYZVector = (v.x, `0.0`, v.z)
  def directions: ISeq[XZSide] = directionLengths.keys.indexed
  def directionLengths: Map[XZSide, PositiveReal] = Seq(
    when(v.x > 0)(East  ->  v.x),
    when(v.x < 0)(West  -> -v.x),
    when(v.z > 0)(South ->  v.z),
    when(v.z < 0)(North -> -v.z),
  ).flatten.toMap.mapVals(PositiveReal)

extension(v: XYZVector)
  def directions: ISeq[XYZSide] = directionLengths.keys.indexed
  def directionLengths: Map[XYZSide, PositiveReal] = Seq(
    when(v.x > 0)(East  ->  v.x),
    when(v.x < 0)(West  -> -v.x),
    when(v.y > 0)(Up    ->  v.y),
    when(v.y < 0)(Down  -> -v.y),
    when(v.z > 0)(South ->  v.z),
    when(v.z < 0)(North -> -v.z),
  ).flatten.toMap.mapVals(PositiveReal)

package farseek.game

import farseek.util.*
import farseek.util.imports.*

type Box[P] = Interval[P]
type YBox[Y] = Box[Y]
type XZBox[X, Z] = (Box[X], Box[Z])
type XYZBox[X, Y, Z] = (Box[X], Box[Y], Box[Z])

type GridBox[P] = DiscreteInterval[P]
type YGridBox[Y] = GridBox[Y]
type XZGridBox[X, Z] = (GridBox[X], GridBox[Z])
type XYZGridBox[X, Y, Z] = (GridBox[X], GridBox[Y], GridBox[Z])

type FreeBox[P] = ContinuousInterval[P]
type YFreeBox[Y] = FreeBox[Y]
type XZFreeBox[X, Z] = (FreeBox[X], FreeBox[Z])
type XYZFreeBox[X, Y, Z] = (FreeBox[X], FreeBox[Y], FreeBox[Z])

extension[Y, B <: YBox[Y]](box: into[B])
  def yBounds: B = box

extension[X, Z, B <: XZBox[X, Z]](box: into[B])
  def xzBounds: B = box

extension[X, Y, Z, B <: XYZBox[X, Y, Z]](box: into[B])
  def xyzBounds: B = box

extension[X, Z](box: into[XZBox[X, Z]])
  def minX: X = box.x.min
  def maxX: X = box.x.max
  def minZ: Z = box.z.min
  def maxZ: Z = box.z.max
  def min: (X, Z) = (minX, minZ)
  def max: (X, Z) = (maxX, maxZ)
  def center(using Interpolation[(X, Z)]): (X, Z) = midpoint(min, max)
  def corners: NonEmptySeq[(X, Z)] = NonEmptySeq(
    (minX, minZ), (minX, maxZ), (maxX, minZ), (maxX, maxZ))

extension[X, Y, Z](box: into[XYZBox[X, Y, Z]])
  def minX: X = box.x.min
  def maxX: X = box.x.max
  def minY: Y = box.y.min
  def maxY: Y = box.y.max
  def minZ: Z = box.z.min
  def maxZ: Z = box.z.max
  def min: (X, Y, Z) = (minX, minY, minZ)
  def max: (X, Y, Z) = (maxX, maxY, maxZ)
  def center(using Interpolation[(X, Y, Z)]): (X, Y, Z) = midpoint(min, max)

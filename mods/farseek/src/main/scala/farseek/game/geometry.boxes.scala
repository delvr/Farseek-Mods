package farseek.game

import farseek.util.*

type CoordBox[P] = Interval[P]
type XZBox[X, Z] = (CoordBox[X], CoordBox[Z])
type XYZBox[X, Y, Z] = (CoordBox[X], CoordBox[Y], CoordBox[Z])

type GridBox[P] = DiscreteInterval[P]
type XZGridBox[X, Z] = (GridBox[X], GridBox[Z])
type XYZGridBox[X, Y, Z] = (GridBox[X], GridBox[Y], GridBox[Z])

type FreeBox[P] = ContinuousInterval[P]
type XZFreeBox[X, Z] = (FreeBox[X], FreeBox[Z])
type XYZFreeBox[X, Y, Z] = (FreeBox[X], FreeBox[Y], FreeBox[Z])

extension[X, Z](box: into XZBox[X, Z])
  def minX: X = box.x.min
  def maxX: X = box.x.max
  def minZ: Z = box.z.min
  def maxZ: Z = box.z.max
  def min: (X, Z) = (minX, minZ)
  def max: (X, Z) = (maxX, maxZ)
  def center(using Interpolation[(X, Z)]): (X, Z) = midpoint(min, max)
  def corners: NonEmptySeq[(X, Z)] = NonEmptySeq(
    (minX, minZ), (minX, maxZ), (maxX, minZ), (maxX, maxZ))

extension[X, Y, Z](box: into XYZBox[X, Y, Z])
  def minX: X = box.x.min
  def maxX: X = box.x.max
  def minY: Y = box.y.min
  def maxY: Y = box.y.max
  def minZ: Z = box.z.min
  def maxZ: Z = box.z.max
  def min: (X, Y, Z) = (minX, minY, minZ)
  def max: (X, Y, Z) = (maxX, maxY, maxZ)
  def center(using Interpolation[(X, Y, Z)]): (X, Y, Z) = midpoint(min, max)

package farseek.game

import farseek.util.*
import farseek.util.imports.*

extension[X, Z](p: into[(X, Z)])
  def x: X = p._1
  def z: Z = p._2
  def xz: (X, Z) = p
  def at[Y](y: Y): (X, Y, Z) = (x, y, z)

extension[X, Y, Z](p: into[(X, Y, Z)])
  def x: X = p._1
  def y: Y = p._2
  def z: Z = p._3
  def xz: (X, Z) = (x, z)
  def xyz: (X, Y, Z) = p
  def at(y: Y): (X, Y, Z) = (x, y, z)
  def at(x: X, z: Z): (X, Y, Z) = (x, y, z)

given [X, Y, Z] => (X, Y, Z) Into Y = _.y
given [X, Y, Z] => (X, Y, Z) Into (X, Z) = _.xz

given [A, X, Y, Z] => (A Into (X, Z), A Into Y) => A Into (X, Y, Z) =
  a => a.into[(X, Z)].at(a.into[Y])

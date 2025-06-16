package farseek.game

extension[X, Z](p: into (X, Z))
  def x: X = p._1
  def z: Z = p._2
  def at[Y](y: Y): (X, Y, Z) = (x, y, z)

extension[X, Y, Z](p: into (X, Y, Z))
  def x: X = p._1
  def y: Y = p._2
  def z: Z = p._3
  def xz: (X, Z) = (x, z)
  def at(y: Y): (X, Y, Z) = (x, y, z)
  def at(x: X, z: Z): (X, Y, Z) = (x, y, z)

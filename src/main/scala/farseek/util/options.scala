package farseek.util

import farseek.util.imports.*

export scala.Option.when // here and not in imports.scala, because overloads must be in same file
@aka("flatWhen")
def when[T](cond: Boolean)(option: => Option[T]): Option[T] = if cond then option else None

extension[A](a: A)
  def when[B](p: A => Boolean)(f: => B): Option[B] = Option.when(p(a))(f)

extension[A](option: Option[A])
  def as[B](b: B): Option[B] = option.map(_ => b)
  def getOrThrow(msg: String): A = option.getOrElse(sys.error(msg))

extension[A](x: A)
  def someWhen(cond:   Boolean): Option[A] = Option.when(cond)(x)
  def someWhen(p: A => Boolean): Option[A] = Option.when(p(x))(x)

  def when[B](cond:   Boolean)(f: A => B): Option[B] = someWhen(cond).map(f)
  def when[B](p: A => Boolean)(f: A => B): Option[B] = someWhen(p(x)).map(f)

  def transformWhen(cond:   Boolean)(f: A => A): A = if cond then f(x) else x
  def transformWhen(p: A => Boolean)(f: A => A): A = if p(x) then f(x) else x

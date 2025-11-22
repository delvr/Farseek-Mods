package farseek.util

import farseek.util.imports.*

infix type NotA[A, B] = NotGiven[A <:< B]

extension[A, B <: A](B: A => B)
  def op(op1: A => A)(b: B): B = B(op1(b))
  def op(op2: (A, A) => A)(b1: B, b2: B): B = B(op2(b1, b2))
  def op[T](op3: (T, A, A) => A)(x: T, b1: B, b2: B): B = B(op3(x, b1, b2))

extension[T](x: T)
  infix def is(p: T => Boolean): Boolean = p(x)
  def tap(sideEffect: T => Unit): T = { sideEffect(x); x }

def badInput   (msg: String): Nothing = throw IllegalArgumentException(msg)
def badState   (msg: String): Nothing = throw IllegalStateException(msg)
def unsupported(msg: String): Nothing = throw UnsupportedOperationException(msg)

extension(element: Any) infix def notIn(container: Any): Nothing =
  throw new NoSuchElementException(s"${element.toString} not found in ${container.toString}")

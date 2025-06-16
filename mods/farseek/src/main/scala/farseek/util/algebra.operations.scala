package farseek.util

import farseek.util.imports.*

@sam trait Negation[T] extends (T => T):
  extension(x: T)
    def unary_- : T = apply(x)
    def negated : T = apply(x)

@sam trait Addition[T] extends ((T, T) => T):
  extension(a: T)
    def + (b: T): T = apply(a, b)
    def ++(b: T): T = apply(a, b)

@sam trait Multiplication[T] extends ((T, T) => T):
  extension(a: T)
    def * (b: T): T = apply(a, b)
    def **(b: T): T = apply(a, b)

/** [[https://en.wikipedia.org/wiki/Left_and_right_(algebra) left and right]] addition */
@sam infix trait ShiftedBy[P, -D] extends ((P, D) => P):
  extension(p: P)
    def :+ (d: into D): P = apply(p, d)
    def +: (d: into D): P = apply(p, d)
    def ::+(d: into D): P = apply(p, d)
    def +::(d: into D): P = apply(p, d)

/** [[https://en.wikipedia.org/wiki/Left_and_right_(algebra) left and right]] multiplication */
@sam infix trait ScaledBy[V, -S] extends ((V, S) => V):
  extension(v: V)
    def :* (s: into S): V = apply(v, s)
    def *: (s: into S): V = apply(v, s)
    def ::*(s: into S): V = apply(v, s)
    def *::(s: into S): V = apply(v, s)

@sam infix trait SeparatedBy[-P, +D] extends ((P, P) => D):
  extension(p: P)
    def -(q: P): D = apply(p, q)
    infix def delta(q: P): D = q - p

type Subtraction[T] = T SeparatedBy T

extension[P, D](p: into P)(using P ShiftedBy D, Negation[D])
  def :-(d: into D): P = p :+ -d

given additionFromShifting: [T: Addition] => T ShiftedBy T = _ + _
given multiplicationFromScaling: [T: Multiplication] => T  ScaledBy T = _ * _
given subtractionFromAddingNegated: [T: {Addition, Negation}] => Subtraction[T] = _ + -_

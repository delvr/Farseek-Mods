package farseek.util

import farseek.util.imports.*

// Workaround to overloading rules so we can define `apply` extensions in different source files
@sam trait Applicable[-A, -P, +B]:
  protected def applyTo(a: A, p: P): B
  extension(a: into[A]) def apply(p: P): B = applyTo(a, p)

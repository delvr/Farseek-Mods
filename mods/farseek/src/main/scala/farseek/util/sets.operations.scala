package farseek.util

import farseek.util.imports.*

@sam trait Union[S] extends ((S, S) => S):
  extension(s1: S) def |(s2: S): S = apply(s1, s2)

@sam trait Intersection[S] extends ((S, S) => S):
  extension(s1: S) def &(s2: S): S = apply(s1, s2)

@sam trait SetDifference[S] extends ((S, S) => S):
  extension(s1: S) def \(s2: S): S = apply(s1, s2)

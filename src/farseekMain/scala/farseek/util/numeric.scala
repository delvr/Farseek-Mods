package farseek.util

import farseek.util.imports.*

type Continuous[P] =  Interpolation[P]
type Discrete  [P] = SeqEnumeration[P]
type ContinuousOrDiscrete[P] = Continuous[P] | Discrete[P]

given Boolean Into JBoolean = java.lang.Boolean.valueOf
given Int     Into JInteger = java.lang.Integer.valueOf
given Long    Into JLong    = java.lang.Long.valueOf
given Float   Into JFloat   = java.lang.Float.valueOf
given Double  Into JDouble  = java.lang.Double.valueOf

given JBoolean Into Boolean = _.booleanValue
given JInteger Into Int     = _.intValue
given JLong    Into Long    = _.longValue
given JFloat   Into Float   = _.floatValue
given JDouble  Into Double  = _.doubleValue

package farseek.util

object imports:
  // Java
  export java.lang.FunctionalInterface as sam // single-abstract-method
  export java.lang.Math.*
  export java.lang.{Boolean as JBoolean, Integer as JInteger, Long as JLong, Float as JFloat, Double as JDouble}
  export java.util.{Collection as JCollection, List as JList, Set as JSet, Map as JMap, Optional as JOption}
  export java.util.Collections.*
  export java.util.PriorityQueue
  export java.util.function.{Function as JFunction, Predicate as JPredicate, Consumer as JConsumer, Supplier as JSupplier}
  export java.util.Random as JRandom
  export java.util.stream.Stream as JStream
  // Scala
  export scala.annotation.{nowarn, tailrec, unused, targetName as aka}
  export scala.collection.{Iterable as AnyIterable, Seq as AnySeq, Set as AnySet, Map as AnyMap, IndexedSeqView}
  export scala.collection.mutable.{Buffer as MutableSeq, ArrayBuffer as MutableISeq, Map as MutableMap, Set as MutableSet}
  export scala.compiletime.asMatchable
  export scala.jdk.CollectionConverters.*
  export scala.jdk.FunctionConverters.*
  export scala.jdk.OptionConverters.*
  export scala.jdk.StreamConverters.*
  export scala.math.Numeric.*
  export scala.math.Numeric.Implicits.*
  export scala.math.Ordering.Implicits.*
  export scala.util.{Try, Success, Failure, Random, NotGiven}

package farseek.util

import farseek.util.imports.*

final case class Graph[V](edges: V => Map[V, NonNegativeReal]):
  type Edge = (V, V)
  type Path = ISeq[Edge]
  type PathPredecessors = Map[V, V]

  extension(source: V)
    /** [[https://en.wikipedia.org/wiki/Uniform_cost_search uniform cost search]] */
    def shortestPathPredecessors(done: V => Boolean): PathPredecessors =
      val predecessors = MutableMap[V, V]()
      val pathCostTo   = MutableMap[V, NonNegativeReal](source -> `0.0`)
      val visited      = MutableSet[V]()
      val toVisit = mutablePriorityQueue(source)(using Ordering.by(pathCostTo))
      while toVisit.nonEmpty do
        val here = toVisit.poll()
        visited += here
        if done(here) then
          toVisit.clear()
        else for (there, edgeCostToThere) <- edges(here) if !visited(there) do
          val pathCostToThere = pathCostTo(here) ++ edgeCostToThere
          if pathCostTo.get(there).forall(_ > pathCostToThere) then
            pathCostTo(there) = pathCostToThere
            predecessors(there) = here
            toVisit.upsert(there)
      end while
      predecessors.toMap

    def shortestPathPredecessorsOf(destinations: V*): PathPredecessors =
      val toReach = MutableSet.from(destinations)
      shortestPathPredecessors(v => (toReach -= v).isEmpty)

    def shortestPathsTo(destinations: V*): Map[V, Path] =
      pathsTo(destinations*)(shortestPathPredecessorsOf(destinations*))

    def shortestPathTo(destination: V): Option[Path] =
      shortestPathsTo(destination).get(destination)

    def pathsTo(destinations: V*)(preds: PathPredecessors): Map[V, Path] =
      destinations.flatMap(dest => pathTo(dest)(preds).map(dest -> _)).toMap

    def pathTo(destination: V)(preds: PathPredecessors): Option[Path] =
      if destination == source then Some(EmptySeq)
      else for
        pred <- preds.get(destination)
        path <- pathTo(pred)(preds)
      yield path :+ (pred -> destination)

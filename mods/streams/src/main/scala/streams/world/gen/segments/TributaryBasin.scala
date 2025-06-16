package streams.world.gen
package segments

import farseek.game.{*, given}
import farseek.util.{*, given}
import farseek.game.imports.*

/** A river [[https://en.wikipedia.org/wiki/Tributary tributary]] or
  * [[https://en.wikipedia.org/wiki/River_source source]]. */
final case class TributaryBasin(generator: StreamsGenerator, basinPos: BasinXZ,
    downstreamSide: XZSide)(using RandomState)
    extends BasinSegment, UpstreamSegment:

  override protected def outletSlopes: NonEmptySeq[(Int, Int)] = unsupported("outletSlopes")

  override lazy val outlet: ChannelAtBorder = outletNode.outlet

  override def generateChunkData: Map[ChunkXZ, StreamsChunk] =
    pathNodes.values.flatMap(_.generateChunkData).toMap

  lazy val (outletNode: TributaryOutletNode, pathNodes: Map[ChunkXZ, TributaryNode]) =
    val chunkPositions = chunkBounds.elements.toSet
    val chunkMaxSurfaceLevels: Map[ChunkXZ, BlockY] = chunkPositions.mappedTo: p =>
      p.into[BlockXZBox].borderElements.map(generator.maxSurfaceAt).min
    val outletNodePos = random.elementOf:
      chunkBounds.facet(downstreamSide).tail.assumedNonEmpty.minsBy(chunkMaxSurfaceLevels)
    val springPositions = chunkBounds.facets.filterByKey(_ != downstreamSide)
      .values.flatten.toSeq.sortBy(chunkMaxSurfaceLevels).takeRight(2)
    def graphEdges(p: ChunkXZ): Map[ChunkXZ, NonNegativeReal] =
      val maxLevel = chunkMaxSurfaceLevels(p)
      p.neighbors4.filter(chunkBounds.contains).mappedTo: neighbor =>
        PositiveInt.clamp(chunkMaxSurfaceLevels(neighbor) delta maxLevel).toReal
    given Graph[ChunkXZ] = Graph(graphEdges)
    val pathPredPositions = outletNodePos.shortestPathPredecessorsOf(springPositions*)
    val upstreamPositions = outletNodePos.pathsTo(springPositions*)(pathPredPositions)
      .values.flatMap(_.map(_._2)).toSet
    val upstreamNodes = upstreamPositions.mappedTo(p =>
      val predPos = pathPredPositions(p)
      val downstreamSide = XZSides.find(n => (p :+ n) == predPos).get
      TributaryUpstreamNode(this, p, chunkMaxSurfaceLevels(p), downstreamSide))
    val outletNode = TributaryOutletNode(this, outletNodePos)
    (outletNode, upstreamNodes + (outletNodePos -> outletNode))

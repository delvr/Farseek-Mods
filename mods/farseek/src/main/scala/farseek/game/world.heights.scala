package farseek.game

import farseek.game.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.Heightmap.Types.*

case class LevelHeight(getMinY: Int, getHeight: PositiveInt) extends LevelHeightAccessor

extension(heights: LevelHeightAccessor)
  def yBounds: BlockYBox = BlockY(heights.getMinY) <-> BlockY(heights.getMaxY)

given (heights: LevelHeightAccessor) => BlockYBox = heights.yBounds

extension(y: Int)(using BlockYBox)
  def yOption: Option[BlockY] = BlockY(y).someWhenInBounds

extension(p: into BlockXZ)(using chunks: BoundedChunkGetter)
  def topSolid:                Option[BlockY] = top(OCEAN_FLOOR)
  def topWorldGenSolid:        Option[BlockY] = top(OCEAN_FLOOR_WG)
  def topNonEmpty:             Option[BlockY] = top(WORLD_SURFACE)
  def topWorldGenNonEmpty:     Option[BlockY] = top(WORLD_SURFACE_WG)
  def topSolidOrLiquid:        Option[BlockY] = top(MOTION_BLOCKING)
  def topNonLeafSolidOrLiquid: Option[BlockY] = top(MOTION_BLOCKING_NO_LEAVES)
  /** [[https://minecraft.wiki/w/Heightmap heightmap]] */
  def top(mapType: Heightmap.Types): Option[BlockY] =
    val chunk = chunks.chunkAt(p)
    require(chunk.getPersistedStatus.heightmapsAfter.contains(mapType))
    given BlockYBox = chunk.yBounds
    p.usingXZ(chunk.getHeight(mapType, _, _)).yOption

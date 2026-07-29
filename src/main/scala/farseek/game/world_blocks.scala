package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.block.Block.*

extension(writer: LevelWriter | Chunk)
  private def defaultFlags: Int = if writer.isInstanceOf[LevelWriter] then UPDATE_ALL else 0
  def update(p: into[BlockPos], state: into[BlockState]): Boolean = set(p, state)
  def set   (p: into[BlockPos], state: into[BlockState]): Boolean = set(p, state, defaultFlags)
  def set   (p: into[BlockPos], state: into[BlockState], flags: Int): Boolean = writer match
    // https://docs.neoforged.net/docs/blocks/states#levelsetblock
    case level: LevelWriter  => level.setBlock     (p, state, flags)
    case chunk: Chunk        => chunk.setBlockState(p, state, flags) != null

extension(p: into[BlockPos])(using writer: LevelWriter | Chunk)
  def setBlock(state: into[BlockState], flags: Flags): Unit =
   writer.set(p, state, flags.toInt): Unit
  def setBlock(state: into[BlockState]): Unit = writer.set(p,        state): Unit
  def removeBlock()(using  BlockGetter): Unit = writer.set(p, p.fluidState): Unit

extension(p: BlockXZ)
  def bufferBox(range: NonNegativeInt = `1`): BlockXZBox =
    (p.x.interval.expandBy[Int](range), p.z.interval.expandBy[Int](range))

package repose

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.*
import net.minecraft.world.phys.shapes.*
import repose.world.*

@Mod(ReposeMod.Id) final class ReposeMod

object ReposeMod extends ModCompanion:
  inline override val Id = "repose"

  val SlopingTag: TagKey[Block] = tag(BLOCK, "sloping")

  def shapeWithSlopes(here: BlockPos, reader: BlockGetter, collider: CollisionContext, original: VoxelShape): VoxelShape =
    if collider.entity.exists(_.canUseSlopes) then
      val p = here.xyz
      given BlockGetter = WrappedBlockGetter(reader, p, range = `1`)
      p.collisionShapeWithSlopes(original)
    else original

  def canSlope(here: BlockPos, reader: BlockGetter): Boolean =
    val p = here.xyz
    given BlockGetter = WrappedBlockGetter(reader, p, range = `0`)
    p.canSlope(here.collisionShape)

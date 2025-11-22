package repose.world

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.core.Direction.Axis.*
import net.minecraft.world.entity.ambient.*
import net.minecraft.world.entity.animal.*
import net.minecraft.world.entity.monster.*
import net.minecraft.world.phys.shapes.Shapes.*
import repose.ReposeMod.*

private val StepHeight = 0.5
private val SlabShape = box(0.0, 0.0, 0.0, 1.0, StepHeight, 1.0)
private val EdgeCuts = Map(
  North -> box(0.0, StepHeight, 0.0, 1.0, 1.0, 0.5),
  South -> box(0.0, StepHeight, 0.5, 1.0, 1.0, 1.0),
  West  -> box(0.0, StepHeight, 0.0, 0.5, 1.0, 1.0),
  East  -> box(0.5, StepHeight, 0.0, 1.0, 1.0, 1.0),
)

extension(p: into[BlockXYZ])(using BlockGetter)
  def canSlope(baseShape: VoxelShape): Boolean =
    p.blockHas(SlopingTag) &&
    baseShape.max(Y) > StepHeight &&
    baseShape.spansFullBlock(X, Z) &&
    !p.optionAbove.exists(_.collisionShape.spansFullBlock(X, Z))

  def collisionShapeWithSlopes(baseShape: VoxelShape): VoxelShape =
    if !canSlope(baseShape) then baseShape
    else
      val cuts = XZSides.filter(d => (p :+ d).collisionShape.max(Y) < StepHeight).toSet
      if cuts.isEmpty then baseShape
      else if cuts(North) && cuts(South) then SlabShape
      else if cuts(West ) && cuts(East ) then SlabShape
      else (baseShape \ EdgeCuts.filterByKey(cuts.contains).values.assumedNonEmpty.reduce(_ | _)).optimize
end extension

extension(entity: Entity) def canUseSlopes: Boolean = entity match
  case _: WaterAnimal | _: FlyingAnimal | _: AmbientCreature |
       _: Ghast | _: HappyGhast | _: Phantom => false
  case _: LivingEntity => !entity.isNoGravity && !entity.isSteppingCarefully &&
                           entity.moveDist > 0 && entity.maxUpStep >= StepHeight
  case _ => false

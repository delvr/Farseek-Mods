package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.world.phys.shapes.*
import net.minecraft.world.phys.shapes.BooleanOp.*
import net.minecraft.world.phys.shapes.Shapes.*

given EmptyTest[VoxelShape] = _.isEmpty

val BlockShape = Shapes.block

extension(shape: VoxelShape)
  def spansFullBlock(axes: into[net.minecraft.core.Direction.Axis]*): Boolean = axes.forall(axis =>
    shape.min(axis) == BlockShape.min(axis) && shape.max(axis) == BlockShape.max(axis))

given ShapesUnion: Union[VoxelShape] = join(_, _, OR)
given ShapesIntersection: Intersection[VoxelShape] = join(_, _, AND)
given ShapesDifference: SetDifference[VoxelShape] = join(_, _, ONLY_FIRST)

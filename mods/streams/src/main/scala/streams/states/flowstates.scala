package streams.states

import farseek.game.*
import farseek.game.imports.*
import farseek.util.{*, given}
import net.minecraft.util.Mth
import net.minecraft.world.level.block.state.properties.RotationSegment.*
import net.minecraft.world.phys.*

val FlowSpeed    = IntProperty.positive[BlockState]("flow_speed", max = 3)
val FlowHeight   = IntProperty.positive[BlockState]("flow_height", max = 9)
val FlowRotation = IntProperty.nonNegative[BlockState]("flow_rotation", max = 15)

val RotationVectors: Map[NonNegativeInt, NonZeroXZVector] = FlowRotation.values.mappedTo(rot =>
  Vec3.directionFromRotation(0, convertToDegrees(rot)).assumedRelative.xz.assumedNonZero.normalized)

def rotation015FromYaw(yaw: Real): NonNegativeInt = NonNegativeInt(convertToSegment(yaw.toFloat))

extension(v: NonZeroXZVector)
  def yaw: Real = Real(Mth.wrapDegrees(Mth.atan2(v.z, v.x)*(180/Pi) - 90))

extension(states: into Seq[BlockState])
  def averageFlowVector: XZVector =
    states.flatMap(_.blockProperty(FlowRotation).map(RotationVectors))
      .averageOption[XZVector].getOrElse(XZVector.Zero)
  def averageFlowSpeedOrMax : PositiveInt = averageOrElseMax(FlowSpeed)
  def averageFlowHeightOrMax: PositiveInt = averageOrElseMax(FlowHeight)
  private def averageOrElseMax(prop: IntProperty[BlockState, PositiveInt]): PositiveInt =
    given Rounding = Rounding.TiesToAway
    states.flatMap(_.blockProperty(prop)).averageOption.getOrElse(prop.max)

package streams.fluids

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.sounds.*
import net.minecraft.world.level.block.Blocks.*
import streams.states.*

val MaxFlowDepth = FlowSpeed.max
val MaxFlowSpeedForFreezing = 2 // not 3, or tributaries will freeze

extension(p: into BlockXYZ)(using BlockGetter)
  def hasFlowState: Boolean = p.blockHas(FlowSpeed)

  def flowStatesHereOrElseAdjacent: ISeq[BlockXYZ] =
    if hasFlowState then NonEmptySeq(p) else p.neighbors4.filter(_.hasFlowState)

extension(p: into BlockXYZ)(using BlockGetter)

  def flowSpeed: NonNegativeInt = flowStatesAbove.whenNonEmpty(adjustedSpeed).getOrElse(`0`)

  def fixedFlowVector: Option[NonZeroXZVector] =
    val flowPositions = flowStatesAbove
    flowPositions.mapInto[BlockState].averageFlowVector.whenNonZero: flow =>
      flow :/ PositiveReal(FlowSpeed.max.plusOne - adjustedSpeed(flowPositions))

  def fixedFluidHeight: Option[`(0,1]`] =
    flowStatesAbove.flatMap(_.blockProperty(FlowHeight))
      .map(fraction(_, FlowHeight.max)).averageOption[Real].map(`(0,1]`)

  private def flowStatesAbove: ISeq[BlockXYZ] =
    p.upwards.take(MaxFlowDepth.plusOne).find(_.isDry)
      .map(_.flowStatesHereOrElseAdjacent).getOrElse(EmptySeq)

  private def adjustedSpeed(positions: ISeq[BlockXYZ]): NonNegativeInt =
    if p.fluidIs(FallingFluid) || positions.isEmpty then FlowSpeed.max
    else
      val there = positions.head
      val depth = PositiveInt(distance(p.y, there.y)) // depth = 1 in topmost liquid block
      if depth > MaxFlowDepth then `0` else
        val surfaceSpeed = positions.mapInto[BlockState].averageFlowSpeedOrMax
        if depth > 1 then
          (surfaceSpeed - fraction(depth, MaxFlowDepth).lerp(`0.0`, surfaceSpeed.toReal).rounded)
            .clampOneTo(surfaceSpeed)
        else surfaceSpeed

extension(p: into BlockXYZ)(using BlockGetter, LevelWriter)
  def surfaceServerRandomTick(flowSpeed: PositiveInt): Unit =
    if p.blockIs(ICE) && flowSpeed > MaxFlowSpeedForFreezing then p.setBlock(WATER)

extension(p: into BlockXYZ)(using level: ClientLevel)
  def surfaceClientRandomTick(flowSpeed: PositiveInt)(using random: RandomSource): Unit =
    if (p.blockIs(ICE) || (p.hasWater && p.fluidState.isSource)) && random.among(`64`) then
      level.playLocalSound(p.into[BlockPos], SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS,
        random.nextFloat * 0.25F + (0.25F * flowSpeed), random.nextFloat + 0.5F, false)

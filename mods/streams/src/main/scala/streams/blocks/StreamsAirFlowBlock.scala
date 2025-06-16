package streams.blocks

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.Blocks.*
import net.minecraft.world.level.block.state.*
import net.minecraft.world.level.block.state.BlockBehaviour.*
import net.minecraft.world.level.redstone.*
import streams.fluids.*
import streams.states.*
import streams.StreamsMod.*

object StreamsAirFlowBlock extends AirBlock(Properties.ofFullCopy(AIR).setId(AirFlow).randomTicks):

  private val TickDelay: NonNegativeInt = `1`
  def apply(speed: PositiveInt, height: PositiveInt, rotation: NonNegativeInt): BlockState =
    this.blockStateWith(FlowSpeed -> speed, FlowHeight -> height, FlowRotation -> rotation)

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    Seq(FlowSpeed, FlowHeight, FlowRotation).foreach(_.registerUsing(builder))
    super.createBlockStateDefinition(builder)

  override def neighborChanged(stateHere: BlockState, level: Level, here: BlockPos,
      oldBlockThere: Block, redstoneOrientation: Orientation, isMovedByPiston: Boolean): Unit =
    given Level = level
    val p = here.xyz
    if p.neighbors4.exists(_.isEmptyBlock) then
      p.scheduleBlockTick(TickDelay)

  override def tick(stateHere: BlockState, level: ServerLevel, here: BlockPos, random: RandomSource): Unit =
    given Level = level
    val p = here.xyz
    p.neighbors4.foreach(n => if n.isEmptyBlock then setAverageFlow(n))

  private def setAverageFlow(p: BlockXYZ)(using BlockGetter, LevelWriter): Unit =
    val neighborsWithFlow = p.neighbors4.filter(_.blockHas(FlowRotation))
    if neighborsWithFlow.size > 1 then
      averageFlowState(neighborsWithFlow.mapInto[BlockState]).foreach(p.setBlock)

  override def randomTick(stateHere: BlockState, level: ServerLevel, here: BlockPos, random: RandomSource): Unit =
    given ServerLevel = level
    val p = here.xyz
    stateHere.blockProperty(FlowSpeed).flatMap(PositiveInt.option).foreach: speed =>
      p.below.surfaceServerRandomTick(speed)

  override def animateTick(stateHere: BlockState, level: Level, here: BlockPos, random: RandomSource): Unit =
    given ClientLevel = level.asInstanceOf[ClientLevel]
    given RandomSource = random
    val p = here.xyz
    stateHere.blockProperty(FlowSpeed).flatMap(PositiveInt.option).foreach: speed =>
      p.below.surfaceClientRandomTick(speed)

  def averageFlowState(states: Seq[BlockState]): Option[BlockState] =
    states.averageFlowVector.whenNonZero: v =>
      StreamsAirFlowBlock(states.averageFlowSpeedOrMax, states.averageFlowHeightOrMax, rotation015FromYaw(v.yaw))

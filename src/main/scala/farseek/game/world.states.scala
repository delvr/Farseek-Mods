package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.tags.*
import net.minecraft.world.level.*
import net.minecraft.world.ticks.*
import net.minecraft.world.ticks.TickPriority.*

// https://docs.neoforged.net/docs/blocks/states

given StateAtBlockPos: (blocks: BlockGetter) => BlockPos Into BlockState = blocks.getBlockState
given StateAtBlockXYZ: (BlockGetter) => BlockXYZ Into BlockState = _.into[BlockPos].converted

given BlockStateToBlock: BlockState Into Block = _.block
given FluidStateToFluid: FluidState Into Fluid = _.getType

given BlockToBlockState: Block Into BlockState = _.defaultBlockState
given FluidToFluidState: Fluid Into FluidState = _.defaultFluidState

given FluidStateToBlockState: FluidState Into BlockState = _.createLegacyBlock
given BlockStateToFluidState: BlockState Into FluidState = _.getFluidState

given EmptyTest[BlockState] = _.isEmpty
given EmptyTest[FluidState] = _.isEmpty
//given EmptyTest[Block] = _.into[BlockState].isEmpty
//given EmptyTest[Fluid] = _.into[FluidState].isEmpty

/** [[https://minecraft.wiki/w/Block_states block state]] */
extension(state: into[BlockState])
  def blockState: BlockState = state

  /** [[https://minecraft.wiki/w/Block_states#List_of_fluid_states fluid state]] */
  def fluidState: FluidState = blockState.getFluidState

  /** [[https://minecraft.wiki/w/Block]] */
  def block: Block = blockState.getBlock
  /** [[https://minecraft.wiki/w/Fluid]] */
  def fluid: Fluid = fluidState.getType

  def isAirBlock  : Boolean = blockState.isAir
  def isEmptyBlock: Boolean = blockState.isEmpty
  /** [[https://minecraft.wiki/w/Block_properties/Solid_(legacy)]] */
  def isLegacySolid: Boolean = blockState.isSolid: @nowarn

  def nonAirBlock  : Boolean = !isAirBlock
  def nonEmptyBlock: Boolean = !isEmptyBlock
  def isNonSolid   : Boolean = !isLegacySolid

  def isGranular: Boolean = blockHas(GranularTag)

  def isDry: Boolean = fluidState.isEmpty
  def isWet: Boolean = !isDry

  def hasWater:  Boolean = fluidHas(FluidTags.WATER)
  def hasLava:   Boolean = fluidHas(FluidTags.LAVA)

  def blockIs(block: Block): Boolean = blockState.is(block)
  def fluidIs(fluid: Fluid): Boolean = fluidState.is(fluid)
  def fluidIsStillOrFlowing(that: Fluid): Boolean = fluid.isSame(that)

  def blockHas(tag: TagKey[Block]): Boolean = blockState.is(tag)
  def fluidHas(tag: TagKey[Fluid]): Boolean = fluidState.is(tag)

  def blockHas(prop: BlockProperty[?]): Boolean = prop.isDefinedAt(blockState)
  def fluidHas(prop: FluidProperty[?]): Boolean = prop.isDefinedAt(fluidState)

  def blockIs(prop: BoolProperty[BlockState]): Boolean = blockProperty(prop).contains(true)
  def fluidIs(prop: BoolProperty[FluidState]): Boolean = fluidProperty(prop).contains(true)

  def blockProperty[V](prop: BlockProperty[V]): Option[V] = prop.option(blockState)
  def fluidProperty[V](prop: FluidProperty[V]): Option[V] = prop.option(fluidState)

  def blockStateWith(ps: into[PropKeyValue[BlockState, ?]]*): BlockState = blockState.withProperties(ps*)
  def fluidStateWith(ps: into[PropKeyValue[FluidState, ?]]*): FluidState = fluidState.withProperties(ps*)

/** [[https://minecraft.wiki/w/Tick#Scheduled_tick scheduled tick]] */
extension(p: into[BlockPos])(using BlockGetter, ScheduledTickAccess)
  def scheduleBlockTick(delay: NonNegativeInt = `0`, priority: TickPriority = NORMAL): Unit =
    summon[ScheduledTickAccess].scheduleTick(p, p.block, delay, priority)

  def scheduleFluidTick(delay: NonNegativeInt = `0`, priority: TickPriority = NORMAL): Unit =
    summon[ScheduledTickAccess].scheduleTick(p, p.fluid, delay, priority)

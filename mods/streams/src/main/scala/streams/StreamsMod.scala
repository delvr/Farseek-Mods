package streams

import farseek.*
import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.*
import net.neoforged.neoforge.client.extensions.common.*
import net.neoforged.neoforge.common.NeoForgeMod.*
import net.neoforged.neoforge.fluids.*
import streams.blocks.*
import streams.client.*
import streams.fluids.*
import streams.items.*
import streams.world.gen.*

// https://alcatrazescapee.com/rivers/

@Mod(StreamsMod.Id) final class StreamsMod(using IEventBus):
  subscribe(StreamsMod.Blocks)
  GameBus.addListener(StreamsGenerator.preBuildSurface)
  GameBus.addListener(StreamsGenerator.postBuildSurface)
  GameBus.addListener(StreamsGenerator.chunkLoaded)
  GameBus.addListener(filterStructure)
  GameBus.addListener(filterFeature)
  GameBus.addListener(onItemUse)

object StreamsMod extends ModCompanion:
  inline override val Id = "streams"

  val AirFlow: ResourceKey[Block] = resourceKey(BLOCK, resource("airflow"))
  val FlowEnchantment: ResourceKey[Enchantment] = resourceKey(ENCHANTMENT, resource("flow"))

  private val Blocks = preregister(BLOCK, AirFlow.location.getPath, StreamsAirFlowBlock)

  def fluidExtensions(extensions: IClientFluidTypeExtensions, types: Array[FluidType]): IClientFluidTypeExtensions =
    if types.contains(WATER_TYPE.value) then FluidExtensionsWrapper(extensions) else extensions

  def fluidHeight(reader: BlockGetter, here: BlockPos): JOption[JFloat] =
    given BlockGetter = reader
    here.fixedFluidHeight.map(_.toFloat.into[JFloat]).toJava

  def fluidFlow(reader: BlockGetter, here: BlockPos): JOption[Vec3] =
    given BlockGetter = reader
    here.fixedFlowVector.map(_.into[Vec3]).toJava

  def fluidShouldFreeze(reader: LevelReader, here: BlockPos): Boolean =
    given BlockGetter = reader
    here.flowSpeed <= MaxFlowSpeedForFreezing

  def isValidTreePos(reader: LevelSimulatedReader, here: BlockPos): Boolean =
    given LevelSimulatedReader = reader
    here.isValidTreePos

package streams

import farseek.*
import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.*
import streams.blocks.*
import streams.fluids.*
import streams.items.*
import streams.world.gen.*

// https://alcatrazescapee.com/rivers/

@Mod(StreamsMod.Id) final class StreamsMod(using IEventBus):
  subscribe(StreamsMod.Blocks)
  // https://docs.neoforged.net/docs/concepts/events#ieventbusaddlistener
  GameBus.addListener(preBuildSurface)
  GameBus.addListener(postBuildSurface)
  GameBus.addListener(chunkLoaded)
  GameBus.addListener(filterStructure)
  GameBus.addListener(filterFeature)
  GameBus.addListener(onItemUse)

object StreamsMod extends ModCompanion:
  inline override val Id = "streams"

  val AirFlow: ResourceKey[Block] = resourceKey(BLOCK, resource("airflow"))
  val FlowEnchantment: ResourceKey[Enchantment] = resourceKey(ENCHANTMENT, resource("flow"))

  private val Blocks = preregister(BLOCK, AirFlow.identifier.getPath, StreamsAirFlowBlock)

  def fluidHeight(reader: BlockGetter, here: BlockPos): JOption[JFloat] =
    given BlockGetter = reader
    here.fixedFluidHeight.map(_.toFloat.converted).toJava

  def fluidFlow(reader: BlockGetter, here: BlockPos): JOption[Vec3] =
    given BlockGetter = reader
    here.fixedFlowVector.map(_.converted).toJava

  def fluidShouldFreeze(reader: LevelReader, here: BlockPos): Boolean =
    given BlockGetter = reader
    here.flowSpeed <= MaxFlowSpeedForFreezing

  def isValidTreePos(reader: LevelSimulatedReader, here: BlockPos): Boolean =
    given LevelSimulatedReader = reader
    here.isValidTreePos

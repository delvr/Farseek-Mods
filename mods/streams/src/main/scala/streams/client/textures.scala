package streams.client

import com.mojang.blaze3d.vertex.*
import farseek.game.imports.*
import farseek.game.*
import farseek.util.*
import net.minecraft.client.*
import net.minecraft.world.level.*
import net.neoforged.neoforge.client.extensions.common.*
import net.neoforged.neoforge.fluids.*
import streams.*
import streams.fluids.*
import streams.states.*

// https://docs.neoforged.net/docs/resources/client/textures
// https://minecraft.wiki/w/Resource_pack#Animation
private lazy val flowTextureKeys: Map[(String, Int), ResourceLocation] =
  (for
    path  <- Set("block/water_flow", "block/lava_flow")
    speed <- oneTo(FlowSpeed.max)
  yield (path, speed) -> StreamsMod.resource(s"${path}_$speed")).toMap

//noinspection TypeAnnotation
class FluidExtensionsWrapper(wrapped: IClientFluidTypeExtensions) extends IClientFluidTypeExtensions:
  /**
    ** @see https://docs.neoforged.net/docs/resources/client/textures#animated-textures */
  override def getFlowingTexture(state: FluidState, getter: BlockAndTintGetter, here: BlockPos): ResourceLocation =
    given BlockGetter = getter
    val texture = wrapped.getFlowingTexture(state, getter, here)
    flowTextureKeys.getOrElse(texture.getPath -> here.xyz.flowSpeed, texture)

  override def getTintColor      = wrapped.getTintColor
  override def getStillTexture   = wrapped.getStillTexture
  override def getFlowingTexture = wrapped.getFlowingTexture
  override def getOverlayTexture = wrapped.getOverlayTexture
  override def getTintColor     (stack: FluidStack) = wrapped.getTintColor(stack)
  override def getStillTexture  (stack: FluidStack) = wrapped.getStillTexture(stack)
  override def getFlowingTexture(stack: FluidStack) = wrapped.getFlowingTexture(stack)
  override def getOverlayTexture(stack: FluidStack) = wrapped.getOverlayTexture(stack)
  override def getRenderOverlayTexture(mc: Minecraft) = wrapped.getRenderOverlayTexture(mc)
  override def getStillTexture(state: FluidState, getter: BlockAndTintGetter, here: BlockPos) =
    wrapped.getStillTexture(state, getter, here)
  override def getOverlayTexture(state: FluidState, getter: BlockAndTintGetter, here: BlockPos) =
    wrapped.getOverlayTexture(state, getter, here)
  override def renderFluid(fluidState: FluidState, getter: BlockAndTintGetter, here: BlockPos,
      vertexConsumer: VertexConsumer, blockState: BlockState) =
    wrapped.renderFluid(fluidState, getter, here, vertexConsumer, blockState)

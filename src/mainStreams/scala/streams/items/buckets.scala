package streams.items

import farseek.game.imports.*
import farseek.game.{*, given}
import farseek.util.{*, given}
import net.minecraft.world.item.*
import net.minecraft.world.item.Item.*
import net.minecraft.world.level.ClipContext.Fluid.*
import net.minecraft.world.level.block.*
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*
import streams.StreamsMod.*
import streams.blocks.*
import streams.fluids.*
import streams.states.*

// https://docs.neoforged.net/docs/items/interactionpipeline/#what-happens-when-i-right-click

// https://minecraft.wiki/w/Bucket#Usage
def onItemUse(event: RightClickItem): Unit =
  val stack = event.getItemStack
  stack.getItem match
    case bucket: BucketItem if bucket.nonEmpty =>
      given level: Level = event.getLevel
      stack.enchantmentLevel(FlowEnchantment).foreach: flowSpeed =>
        val player = event.getEntity
        val hitResult = getPlayerPOVHitResult(level, player, ANY)
        if hitResult.isEmpty then
          event.pass()
        else
          val hitPos  = hitResult.hitBlockPos
          val hitSide = hitResult.hitBlockSide
          val posInFrontOfHit = hitPos :+ hitSide
          if !player.mayInteractAt(hitPos) ||
             !player.mayUseItemAt(posInFrontOfHit.converted, hitSide.converted, stack) then
            event.fail()
          else
            val hitState = hitPos.blockState
            val isOrCanHaveFluid = hitState.block match
              case container: LiquidBlockContainer => container.canPlaceLiquid(
                player, level, hitPos.converted, hitState, bucket.content)
              case _ => hitPos.isWet
            val pourPos = if isOrCanHaveFluid then hitPos else posInFrontOfHit
            val currentFluid = pourPos.fluidState
            if currentFluid.isEmpty then
              if !bucket.emptyContents(player, level, pourPos.converted, hitResult, stack) then
                event.fail()
            else if currentFluid.fluidIsStillOrFlowing(bucket.content) then
              bucket.playEmptySound(player, level, pourPos.converted)
            else
              event.pass()
            if !event.isCanceled then
              val p = pourPos.above
              // airBlock includes existing AirFlow blocks
              if p.isAirBlock then p.setBlock(StreamsAirFlowBlock(flowSpeed,
                p.flowStatesHereOrElseAdjacent.mapInto[BlockState].averageFlowHeightOrMax,
                rotation015FromYaw(Real(player.getYRot))))
              event.succeed()
    case _ =>

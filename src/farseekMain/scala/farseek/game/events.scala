package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import net.minecraft.world.*
import net.minecraft.world.InteractionResult.*
import net.neoforged.bus.api.*
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*

// https://docs.neoforged.net/docs/concepts/events
// https://neoforged.net/news/20.2eventbus-changes/
// https://github.com/neoforged/Bus

val GameBus: IEventBus = EVENT_BUS

extension[E <: Event](event: E)
  def post()(using bus: IEventBus = GameBus): Unit = bus.post(event): Unit
  def postThenCheck()(using bus: IEventBus = GameBus): E = bus.post(event)

// https://docs.neoforged.net/docs/concepts/events#cancellable-events
extension(event: ICancellableEvent)
  def cancel(): Unit = event.setCanceled(true)
  def shouldProceed: Boolean = !event.isCanceled

trait CancellableEvent extends Event, ICancellableEvent

trait EventWithResponse[R] extends Event:
  var response: Option[R] = None
  def respond(r: R): Unit = response = Some(r)

// https://docs.neoforged.net/docs/items/interactions/#interactionresult
@sam trait InteractionEvent[-E <: ICancellableEvent] extends ((E, InteractionResult) => Unit):
  extension(event: E)
    def succeed(): Unit = cancelWith(SUCCESS)
    def consume(): Unit = cancelWith(CONSUME)
    def pass():    Unit = cancelWith(PASS)
    def fail():    Unit = cancelWith(FAIL)
    def cancelWith(result: InteractionResult): Unit =
      event.cancel()
      apply(event, result)

given InteractionEvent[EntityInteract]  = _.setCancellationResult(_)
given InteractionEvent[RightClickBlock] = _.setCancellationResult(_)
given InteractionEvent[RightClickItem]  = _.setCancellationResult(_)

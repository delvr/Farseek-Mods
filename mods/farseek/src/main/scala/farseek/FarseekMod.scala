package farseek

import farseek.game.*
import farseek.game.imports.*

// https://docs.neoforged.net/docs/gettingstarted/modfiles#mod-entrypoints
// https://docs.neoforged.net/docs/concepts/events#the-mod-lifecycle

@Mod(FarseekMod.Id) final class FarseekMod

object FarseekMod extends ModCompanion:
  inline override val Id = "farseek"

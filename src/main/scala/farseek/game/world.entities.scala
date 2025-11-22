package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*

// https://minecraft.wiki/w/Entity
// https://docs.neoforged.net/docs/entities/

extension(player: Player)
  def mayInteractAt(p: into[BlockPos]): Boolean = player.level.mayInteract(player, p)

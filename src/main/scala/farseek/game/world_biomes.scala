package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*

// https://minecraft.wiki/w/Biome
// https://minecraft.wiki/w/Biome_definition

extension(p: into[BlockPos])(using level: LevelReader)
  def biome: Holder[Biome] = level.getBiome(p)

// because biome tags tend to start with IS_
given Applicable[Holder[Biome], TagKey[Biome], Boolean] = _.is(_)

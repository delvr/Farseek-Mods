package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*

// https://minecraft.wiki/w/Biome
// https://minecraft.wiki/w/Biome_definition

// https://github.com/ChampionAsh5357/NeoForge-Docs/blob/feat/worldgen-primer/docs/worldgen/biomes/biomemodifiers.md
// https://forge.gemwire.uk/wiki/Biome_Modifiers (legacy Forge)

extension(p: into[BlockPos])(using level: LevelReader)
  def biome: Holder[Biome] = level.getBiome(p)

//extension(p: into[Holder[Biome]])
//  // because biome tags tend to start with IS_
//  def apply(tag: TagKey[Biome]): Boolean = p.is(tag)

// because biome tags tend to start with IS_
given Applicable[Holder[Biome], TagKey[Biome], Boolean] = _.is(_)

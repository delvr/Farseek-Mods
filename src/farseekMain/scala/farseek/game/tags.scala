package farseek.game

import farseek.*
import farseek.game.imports.*
import net.minecraft.tags.*

// https://docs.neoforged.net/docs/resources/server/tags
// https://minecraft.wiki/w/Tag

extension[T](registryKey: RegistryKey[T])
  def tag(namespace: String, path: String): TagKey[T] = TagKey.create(registryKey, resource(namespace, path))
  def commonTag(path: String): TagKey[T] = tag("c", path)

val GranularTag: TagKey[Block] = BLOCK.commonTag("granular")

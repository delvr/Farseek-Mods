package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.*
import net.minecraft.core.*
import net.minecraft.resources.*
import net.minecraft.resources.ResourceLocation.*

// https://docs.neoforged.net/docs/concepts/registries/
// https://docs.neoforged.net/docs/resources/
// https://docs.neoforged.net/docs/misc/resourcelocation
// https://docs.neoforged.net/docs/misc/resourcelocation#resourcekeys
// https://neoforged.net/news/20.2registry-rework/
// https://docs.neoforged.net/docs/concepts/registries#datapack-registries
// https://forge.gemwire.uk/wiki/Datapack_Registries (for legacy Forge 1.19)

type RegistryKey[V] = ResourceKey[? <: Registry[V]]

given holderValue: [V] => Holder[V] Into V = _.value

given ResourceKeyLocation: ResourceKey[?] Into ResourceLocation = _.location

// RegistryAccess <: HolderLookup.Provider <: HolderGetter.Provider
// Registry <: HolderLookup.RegistryLookup <: HolderLookup <: HolderGetter
given [V] => KeyedReader[HolderGetter[V], ResourceKey[V]  , Holder.Reference[V]] = _.get(_).toScala
given [V] => KeyedReader[    Registry[V], ResourceLocation, Holder.Reference[V]] = _.get(_).toScala

given [V] => (level: LevelReader) => ResourceKey[V] Into Holder.Reference[V] =
  key => level.registry(key.registryKey)(key)

extension(level: LevelReader)
  def registry[V](key: into RegistryKey[V]): Registry[V] = level.registryAccess.lookupOrThrow(key)

def resource(namespace: String, path: String): ResourceLocation = fromNamespaceAndPath(namespace, path)

def resourceKey[V](registryKey: RegistryKey[V], location: ResourceLocation): ResourceKey[V] =
  ResourceKey.create(registryKey, location)

def subscribe(registries: DeferredRegister[?]*)(using bus: IEventBus): Unit = registries.foreach(_.register(bus))

package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.core.*
import net.minecraft.resources.*
import net.minecraft.resources.Identifier.*

// https://docs.neoforged.net/docs/concepts/registries/
// https://docs.neoforged.net/docs/misc/identifier

type RegistryKey[V] = ResourceKey[? <: Registry[V]]

given [V] => Holder[V] Into V = _.value

given ResourceKey[?] Into Identifier = _.identifier

// RegistryAccess <: HolderLookup.Provider <: HolderGetter.Provider
// Registry <: HolderLookup.RegistryLookup <: HolderLookup <: HolderGetter
given [V] => KeyedReader[HolderGetter[V], ResourceKey[V], Holder.Reference[V]] = _.get(_).toScala
given [V] => KeyedReader[    Registry[V], Identifier,     Holder.Reference[V]] = _.get(_).toScala

given [V] => (level: LevelReader) => ResourceKey[V] Into Holder.Reference[V] =
  key => level.registry(key.registryKey)(key)

extension(level: LevelReader)
  def registry[V](key: into[RegistryKey[V]]): Registry[V] = level.registryAccess.lookupOrThrow(key)

def resource(namespace: String, path: String): Identifier = fromNamespaceAndPath(namespace, path)

def resourceKey[V](registryKey: RegistryKey[V], identifier: Identifier): ResourceKey[V] =
  ResourceKey.create(registryKey, identifier)

def subscribe(registries: DeferredRegister[?]*)(using bus: IEventBus): Unit = registries.foreach(_.register(bus))

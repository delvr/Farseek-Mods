package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import net.minecraft.resources.Identifier.*
import net.neoforged.neoforge.registries.*

trait ModCompanion:
  def Id: String

  def resource(path: String): Identifier = fromNamespaceAndPath(Id, path)

  def tag[T](registryKey: RegistryKey[T], path: String): TagKey[T] = registryKey.tag(Id, path)

  // https://docs.neoforged.net/docs/concepts/registries#deferredregister
  def preregister[V](registryKey: RegistryKey[V], entries: (String, () => V)*): DeferredRegister[V] =
    val registry = DeferredRegister.create(registryKey, Id)
    entries.foreach((k, v) => registry.register(k, v.asJava))
    registry

  def preregister[V](registryKey: RegistryKey[V], key: String, value: => V): DeferredRegister[V] =
    preregister(registryKey, key -> (() => value))

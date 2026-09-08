package farseek.util

import farseek.util.imports.*

@sam trait KeyedReader[-R, -K, +V] extends ((R, K) => Option[V]):
  private def value(r: R, k: K): Option[V] = apply(r, k) // for internal apply() disambiguation
  extension(r: R)
    @aka("valueOf") def apply(key: into[K]): V = getOrElse(key, key notIn r)
    def getOption(key: into[K]): Option[V] = value(r, key)
    def getOrElse[W >: V](key: into[K], default: => W): W = getOption(key).getOrElse(default)

@sam trait KeyedWriter[-W, -K, -V] extends ((W, K, V) => Unit):
  extension(w: W)
    def update(key: into[K], value: into[V]): Unit = apply(w, key, value)

extension[RW, K, V](rw: RW)(using KeyedReader[RW, K, V], KeyedWriter[RW, K, V])
  def getOrElseUpdate(key: into[K], default: => V): V = rw.getOrElse(key, default.tap(rw(key) = _))

given keyedReaderMembership: [R, K] => (KeyedReader[R, K, ?]) => R CanContain K =
  _.getOption(_).isDefined

final class MapWithInvalidation[-K, V]:
  private val entries     = MutableMap[K, V]()
  private val invalidated = MutableSet[K]()
  def apply (key: into[K]): V = entries(validated(key))
  def option(key: into[K]): Option[V] = entries.get(validated(key))
  def update(key: into[K], value: into[V]): Unit = entries(validated(key)) = value
  def isValid(key: into[K]): Boolean = !invalidated(key)
  def invalidate(key: into[K]): Unit = { entries -= key; invalidated += key }
  private def validated(key: K): K =
    if invalidated(key) then badState(s"${this.toString}: key ${key.toString} was invalidated") else key

extension[K, V](map: AnyMap[K, V])
  def withInvalidation: MapWithInvalidation[K, V] =
    val newMap = MapWithInvalidation[K, V]()
    map.foreach(newMap(_) = _)
    newMap

given [K, V] => KeyedReader[MapWithInvalidation[K, V], K, V] = _.option(_)
given [K, V] => KeyedWriter[MapWithInvalidation[K, V], K, V] = _.update(_, _)

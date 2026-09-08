package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import farseek.util.{*, given}
import net.minecraft.world.level.block.state.*
import net.minecraft.world.level.block.state.properties.*

type PropHolder[S] = StateHolder[?, S]

trait StateProperty[S <: PropHolder[S], V] extends (S IntoOption V):
  protected type W <: Comparable[W]
  protected def wrapped: Property[W]
  protected def convert(value: V): W
  protected def convert(value: W): V
  final override def isDefinedAt(s: S): Boolean = s.hasProperty(wrapped)
  final override def apply(s: S): V = convert(s.getValue(wrapped))
  final def setValue(s: S, v: V): S = s.setValue(wrapped, convert(v))

final case class BoolProperty[S <: PropHolder[S]](
    wrapped: BooleanProperty) extends StateProperty[S, Boolean]:
  override protected type W = JBoolean
  override protected def convert(value:  Boolean) = value.converted
  override protected def convert(value: JBoolean) = value.converted

final case class IntProperty[S <: PropHolder[S], V <: NonNegativeInt: Discrete](
    wrapped: IntegerProperty, refined: Int IntoOption V) extends StateProperty[S, V]:
  override protected type W = JInteger
  lazy val min: V = refined(wrapped.min)
  lazy val max: PositiveInt = PositiveInt(wrapped.max)
  lazy val values: NonEmptySeq[V] = min towards refined(max)
  override protected def convert(value: V): JInteger = value.converted
  override protected def convert(value: JInteger): V = refined(value.converted)

object BoolProperty:
  def apply[S <: PropHolder[S]](name: String): BoolProperty[S] =
    BoolProperty(BooleanProperty.create(name))

object IntProperty:
  def nonNegative[S <: PropHolder[S]](wrapped: IntegerProperty): IntProperty[S, NonNegativeInt] =
    IntProperty(wrapped, NonNegativeInt)
  def positive[S <: PropHolder[S]](wrapped: IntegerProperty): IntProperty[S, PositiveInt] =
    IntProperty(wrapped, PositiveInt)
  def nonNegative[S <: PropHolder[S]](name: String, max: Int): IntProperty[S, NonNegativeInt] =
    IntProperty(IntegerProperty.create(name, 0, max), NonNegativeInt)
  def positive[S <: PropHolder[S]](name: String, max: Int): IntProperty[S, PositiveInt] =
    IntProperty(IntegerProperty.create(name, 1, max), PositiveInt)

type BlockProperty[V] = StateProperty[BlockState, V]
type FluidProperty[V] = StateProperty[FluidState, V]

final case class PropKeyValue[S <: PropHolder[S], V](key: StateProperty[S, V], value: into[V])

given [S <: PropHolder[S], V] => (StateProperty[S, V], V) Into PropKeyValue[S, V] = PropKeyValue(_, _)

extension[S <: PropHolder[S]](s: S)
  def withProperty[V](prop: StateProperty[S, V], v: V): S = prop.setValue(s, v)

  def withProperties(props: into[PropKeyValue[S, ?]]*): S = props.foldLeft(s):
    (s, p) => s.withProperty(p.key, p.value)

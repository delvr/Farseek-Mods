package farseek.game

import farseek.game.imports.*
import farseek.util.*
import net.minecraft.world.phys.shapes.*

given EmptyTest[CollisionContext] = _ == CollisionContext.empty

extension(context: CollisionContext)
  def entity: Option[Entity] = context match
    case obs: EntityCollisionContext => Option(obs.getEntity)
    case _ => None

extension(p: into BlockPos)(using reader: BlockGetter)
  def outlineShape  : VoxelShape = p.blockState.getShape(reader, p)
  def collisionShape: VoxelShape = p.blockState.getCollisionShape(reader, p)
  def outlineShapeFor  (observer: CollisionContext): VoxelShape = p.blockState.getShape(reader, p, observer)
  def collisionShapeFor(collider: CollisionContext): VoxelShape = p.blockState.getCollisionShape(reader, p, collider)
  def isVisibleTo (observer: CollisionContext): Boolean =   outlineShapeFor(observer).nonEmpty
  def isTangibleTo(collider: CollisionContext): Boolean = collisionShapeFor(collider).nonEmpty

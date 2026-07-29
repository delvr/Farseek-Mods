package farseek.game

import farseek.game.imports.*
import farseek.util.imports.*
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.entity.animal.bee.Bee
import net.minecraft.world.entity.animal.fish.WaterAnimal
import net.minecraft.world.entity.animal.happyghast.HappyGhast
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus
import net.minecraft.world.entity.animal.parrot.Parrot
import net.minecraft.world.entity.monster.*

// https://minecraft.wiki/w/Entity
// https://docs.neoforged.net/docs/entities/
// https://docs.neoforged.net/docs/entities/livingentity

private val AquaticEntityClasses =
  Set(classOf[WaterAnimal], classOf[AbstractNautilus], classOf[Guardian])
private val AerialEntityClasses =
  Set(classOf[Bat], classOf[Bee], classOf[Parrot], classOf[Ghast], classOf[HappyGhast])

extension(entity: Entity)
  def isAquatic: Boolean = AquaticEntityClasses.exists(entity.getClass.isAssignableFrom)
  def isAerial : Boolean =  AerialEntityClasses.exists(entity.getClass.isAssignableFrom)

extension(player: Player)
  def mayInteractAt(p: into[BlockPos]): Boolean = player.level.mayInteract(player, p)

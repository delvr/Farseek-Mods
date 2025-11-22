package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.world.item.*

// https://docs.neoforged.net/docs/items/
// https://minecraft.wiki/w/Item
// https://minecraft.wiki/w/Enchanting
// https://minecraft.wiki/w/Tutorials/Adding_custom_enchantments
// https://gist.github.com/ChampionAsh5357/d895a7b1a34341e19c80870720f9880f#the-enchantment-datapack-object

given EmptyTest[Item] = _ == Items.AIR
given EmptyTest[ItemStack] = _.isEmpty

given Item Into ItemStack = ItemStack(_)

extension(stack: into[ItemStack])
  def item: Item = stack.getItem

  def enchantmentLevel(enchantment: into[Holder[Enchantment]]): Option[PositiveInt] =
    PositiveInt.option(stack.getEnchantmentLevel(enchantment))

  def withEnchantment(enchantment: into[Holder[Enchantment]], level: PositiveInt = `1`): ItemStack =
    stack.enchant(enchantment, level); stack

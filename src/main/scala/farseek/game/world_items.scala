package farseek.game

import farseek.game.imports.*
import farseek.util.*
import farseek.util.imports.*
import net.minecraft.world.item.*

// https://docs.neoforged.net/docs/items/
// https://docs.neoforged.net/docs/resources/server/enchantments/

given EmptyTest[Item] = _ == Items.AIR
given EmptyTest[ItemStack] = _.isEmpty

given Item Into ItemStack = ItemStack(_)

// https://docs.neoforged.net/docs/items/#itemstacks
extension(stack: into[ItemStack])
  def item: Item = stack.getItem

  def enchantmentLevel(enchantment: into[Holder[Enchantment]]): Option[PositiveInt] =
    PositiveInt.option(stack.getEnchantmentLevel(enchantment))

  def withEnchantment(enchantment: into[Holder[Enchantment]], level: PositiveInt = `1`): ItemStack =
    stack.enchant(enchantment, level); stack

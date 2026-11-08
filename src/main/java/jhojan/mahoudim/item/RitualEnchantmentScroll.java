package jhojan.mahoudim.item;

import jhojan.mahoudim.enchantment.RitualEnchantment;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** Standard scroll that grants a level-I enchanted book from its matching ritual. */
public final class RitualEnchantmentScroll extends RitualRewardScroll {
   private final RitualEnchantment enchantment;

   public RitualEnchantmentScroll(RitualEnchantment enchantment) {
      this.enchantment = enchantment;
   }

   @Override
   protected ItemStack createReward(Player player) {
      Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
      ResourceKey<Enchantment> key = ResourceKey.create(
         Registries.ENCHANTMENT,
         ResourceLocation.fromNamespaceAndPath("mahoudim", this.enchantment.id)
      );
      Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
      return holder == null
         ? ItemStack.EMPTY
         : EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, 1));
   }

   @Override
   protected Component claimedMessage() {
      return Component.translatable(
         "message.mahoudim.enchantment_scroll_claimed",
         Component.translatable("enchantment.mahoudim." + this.enchantment.id)
      );
   }
}

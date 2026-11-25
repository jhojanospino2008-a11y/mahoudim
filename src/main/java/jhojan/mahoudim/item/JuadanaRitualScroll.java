package jhojan.mahoudim.item;

import jhojan.mahoudim.Reg;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class JuadanaRitualScroll extends RitualRewardScroll {
   @Override
   protected ItemStack createReward(Player player) {
      return new ItemStack((ItemLike)Reg.JUADANA.get());
   }

   @Override
   protected Component claimedMessage() {
      return Component.translatable("message.mahoudim.juadana_ritual_claimed");
   }
}

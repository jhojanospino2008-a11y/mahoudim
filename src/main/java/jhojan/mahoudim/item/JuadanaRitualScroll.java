package jhojan.mahoudim.item;

import java.util.List;
import jhojan.mahoudim.Reg;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import stepsword.mahoutsukai.config.MTConfig;
import stepsword.mahoutsukai.datacomponents.scrollmahou.ScrollMahou;
import stepsword.mahoutsukai.item.spells.SpellScroll;

/** Ritual reward that is read once to claim the Juadana. */
public final class JuadanaRitualScroll extends SpellScroll {
   public JuadanaRitualScroll() {
      super(new Item.Properties().stacksTo(1));
   }

   @Override
   public int getInitialManaCost() {
      return 0;
   }

   @Override
   public void useAction(ItemStack stack, Level level, LivingEntity entity, boolean consume) {
      if (!consume || !(entity instanceof Player player)) {
         return;
      }
      if (player.isCreative() && !MTConfig.CREATIVE_MODE_SPELLS) {
         return;
      }
      super.useAction(stack, level, entity, true);
   }

   @Override
   public boolean doSpell(Player user, ScrollMahou mahou, ItemStack stack) {
      if (!(user instanceof ServerPlayer)) {
         return false;
      }
      ItemStack reward = new ItemStack(Reg.JUADANA.get());
      if (!user.getInventory().add(reward)) {
         user.drop(reward, false);
      }
      user.displayClientMessage(Component.translatable("message.mahoudim.juadana_ritual_claimed"), true);
      return true;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.juadana_ritual_scroll.tooltip"));
   }
}

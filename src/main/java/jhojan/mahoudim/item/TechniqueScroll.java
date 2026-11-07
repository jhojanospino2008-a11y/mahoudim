package jhojan.mahoudim.item;

import java.util.List;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.TechniqueActions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import stepsword.mahoutsukai.datacomponents.scrollmahou.ScrollMahou;
import stepsword.mahoutsukai.item.spells.SpellScroll;

public class TechniqueScroll extends SpellScroll {
   private final Technique tech;

   public TechniqueScroll(Technique tech) {
      super(tech.id);
      this.tech = tech;
   }

   public Technique technique() {
      return this.tech;
   }

   public int getInitialManaCost() {
      return this.tech.cost();
   }

   public boolean doSpell(Player user, ScrollMahou mahou, ItemStack stack) {
      if (user instanceof ServerPlayer sp && TechniqueActions.cast(this.tech, sp)) {
         return true;
      }

      return false;
   }

   public boolean shouldConsume(Player user, ScrollMahou scrollMahou, boolean consume, ItemStack stack) {
      if (this.tech == Technique.SPATIAL_ANCHOR && user.isShiftKeyDown() && user instanceof ServerPlayer sp) {
         TechniqueActions.markAnchor(sp);
         return false;
      } else {
         return super.shouldConsume(user, scrollMahou, consume, stack);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.scroll_" + this.tech.id + ".tooltip"));
   }
}

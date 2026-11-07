package jhojan.mahoudim.item;

import java.util.List;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.TechniqueActions;
import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import stepsword.mahoutsukai.item.FaeEssence;
import stepsword.mahoutsukai.datacomponents.scrollmahou.ScrollMahou;
import stepsword.mahoutsukai.mana.PlayerManaManager;
import stepsword.mahoutsukai.item.spells.SpellScroll;
import stepsword.mahoutsukai.util.Utils;

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

   @Override
   public void useAction(ItemStack stack, Level level, LivingEntity entity, boolean consume) {
      if (level.isClientSide || !(entity instanceof Player user)) {
         return;
      }
      int cost = this.getInitialManaCost();
      if (SecondSorceryCode.isBeingUsed(user) && cost > 0) {
         cost = Math.max(1, (cost + 1) / 2);
      }
      ScrollMahou scrollMahou = Utils.getScrollMahou(stack);
      if (scrollMahou != null
         && (scrollMahou.getCasterUUID().isEmpty()
            || user.getUUID().equals(scrollMahou.getCasterUUID().get())
            || scrollMahou.getCasterUUID().get().equals(FaeEssence.faeID)
            || user.isCreative())
         && PlayerManaManager.drainMana(user, cost, false, false) == cost
         && this.shouldConsume(user, scrollMahou, consume, stack)) {
         stack.shrink(1);
      }
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

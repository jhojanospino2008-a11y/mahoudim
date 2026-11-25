package jhojan.mahoudim.item;

import java.util.List;
import java.util.UUID;
import jhojan.mahoudim.tech.EffectTechniqueActions;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.TechniqueActions;
import jhojan.mahoudim.tech.UnlimitedProjectionSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import stepsword.mahoutsukai.datacomponents.scrollmahou.ScrollMahou;
import stepsword.mahoutsukai.item.FaeEssence;
import stepsword.mahoutsukai.item.spells.SpellScroll;
import stepsword.mahoutsukai.mana.PlayerManaManager;
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

   public void useAction(ItemStack stack, Level level, LivingEntity entity, boolean consume) {
      if (!level.isClientSide && entity instanceof Player user) {
         if (this.tech == Technique.UNLIMITED_PROJECTION && user.isShiftKeyDown()) {
            if (user instanceof ServerPlayer serverPlayer) {
               UnlimitedProjectionSpell.clearMemory(serverPlayer, stack);
            }
         } else if (this.tech == Technique.UNLIMITED_PROJECTION) {
            if (user instanceof ServerPlayer serverPlayer) {
               ScrollMahou scrollMahou = Utils.getScrollMahou(stack);
               if (scrollMahou != null
                  && (
                     scrollMahou.getCasterUUID().isEmpty()
                        || user.getUUID().equals(scrollMahou.getCasterUUID().get())
                        || ((UUID)scrollMahou.getCasterUUID().get()).equals(FaeEssence.faeID)
                        || user.isCreative()
                  )) {
                  UnlimitedProjectionSpell.CastPlan plan = UnlimitedProjectionSpell.prepareCast(serverPlayer, stack);
                  if (plan != null) {
                     int projectionCost = this.getInitialManaCost();
                     if (SecondSorceryCode.isBeingUsed(user) && projectionCost > 0) {
                        projectionCost = Math.max(1, (projectionCost + 1) / 2);
                     }

                     if (PlayerManaManager.drainMana(user, projectionCost, false, false) == projectionCost) {
                        UnlimitedProjectionSpell.cast(serverPlayer, stack, plan);
                     }
                  }
               }
            }
         } else if (!((this.tech == Technique.EFFECT_INCREMENT || this.tech == Technique.EFFECT_CREATE) && user instanceof ServerPlayer serverPlayerx)
            || EffectTechniqueActions.canBegin(this.tech, serverPlayerx)) {
            int cost = this.getInitialManaCost();
            if (SecondSorceryCode.isBeingUsed(user) && cost > 0) {
               cost = Math.max(1, (cost + 1) / 2);
            }

            ScrollMahou scrollMahou = Utils.getScrollMahou(stack);
            if (scrollMahou != null
               && (
                  scrollMahou.getCasterUUID().isEmpty()
                     || user.getUUID().equals(scrollMahou.getCasterUUID().get())
                     || ((UUID)scrollMahou.getCasterUUID().get()).equals(FaeEssence.faeID)
                     || user.isCreative()
               )
               && PlayerManaManager.drainMana(user, cost, false, false) == cost
               && this.shouldConsume(user, scrollMahou, consume, stack)) {
               stack.shrink(1);
            }
         }
      }
   }

   public boolean doSpell(Player user, ScrollMahou mahou, ItemStack stack) {
      if (user instanceof ServerPlayer sp) {
         return this.tech == Technique.UNLIMITED_PROJECTION ? UnlimitedProjectionSpell.cast(sp, stack) : TechniqueActions.cast(this.tech, sp);
      } else {
         return false;
      }
   }

   public boolean shouldConsume(Player user, ScrollMahou scrollMahou, boolean consume, ItemStack stack) {
      if (this.tech == Technique.UNLIMITED_PROJECTION && !user.isShiftKeyDown()) {
         this.doSpell(user, scrollMahou, stack);
         return false;
      } else if (this.tech == Technique.SPATIAL_ANCHOR && user.isShiftKeyDown() && user instanceof ServerPlayer sp) {
         TechniqueActions.markAnchor(sp);
         return false;
      } else {
         return super.shouldConsume(user, scrollMahou, consume, stack);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.scroll_" + this.tech.id + ".tooltip"));
      if (this.tech == Technique.UNLIMITED_PROJECTION) {
         ItemStack remembered = UnlimitedProjectionSpell.getRememberedStack(stack);
         Component name = (Component)(remembered.isEmpty()
            ? Component.translatable("item.mahoudim.scroll_unlimited_projection.memory_empty")
            : remembered.getHoverName());
         tooltip.add(Component.translatable("item.mahoudim.scroll_unlimited_projection.memory", new Object[]{name}).withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.translatable("item.mahoudim.scroll_unlimited_projection.clear_hint").withStyle(ChatFormatting.DARK_GRAY));
      }
   }
}

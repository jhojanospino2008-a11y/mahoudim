package jhojan.mahoudim.tech;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ComponentItemHandler;
import stepsword.mahoutsukai.config.MTConfig;
import stepsword.mahoutsukai.effects.projection.ProjectionSpellEffect;
import stepsword.mahoutsukai.effects.projection.StrengtheningSpellEffect;
import stepsword.mahoutsukai.entity.WeaponProjectileEntity;
import stepsword.mahoutsukai.util.Utils;
import stepsword.mahoutsukai.util.Utils.MAHOU_SELECTION;

public final class UnlimitedProjectionSpell {
   private static final int MEMORY_SLOTS = 1;

   private UnlimitedProjectionSpell() {
   }

   public static UnlimitedProjectionSpell.CastPlan prepareCast(ServerPlayer player, ItemStack scroll) {
      ItemStack remembered = getRememberedStack(scroll);
      if (remembered.isEmpty()) {
         ItemStack target = findTargetStack(player);
         if (target.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_no_target"), true);
            return null;
         } else {
            return new UnlimitedProjectionSpell.CastPlan(false, target);
         }
      } else if (!isProjectable(remembered, player.level())) {
         new ComponentItemHandler(scroll, DataComponents.CONTAINER, 1).setStackInSlot(0, ItemStack.EMPTY);
         player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_memory_invalid"), true);
         return null;
      } else {
         return new UnlimitedProjectionSpell.CastPlan(true, remembered);
      }
   }

   public static boolean cast(ServerPlayer player, ItemStack scroll) {
      UnlimitedProjectionSpell.CastPlan plan = prepareCast(player, scroll);
      return plan != null && cast(player, scroll, plan);
   }

   public static boolean cast(ServerPlayer player, ItemStack scroll, UnlimitedProjectionSpell.CastPlan plan) {
      ItemStack source = plan.item().copy();
      if (!isProjectable(source, player.level())) {
         if (plan.projection()) {
            new ComponentItemHandler(scroll, DataComponents.CONTAINER, 1).setStackInSlot(0, ItemStack.EMPTY);
            player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_memory_invalid"), true);
         } else {
            player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_no_target"), true);
         }

         return false;
      } else if (!plan.projection()) {
         source.setCount(1);
         new ComponentItemHandler(scroll, DataComponents.CONTAINER, 1).setStackInSlot(0, source);
         player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_memorized", new Object[]{source.getHoverName()}), true);
         player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9F, 1.2F);
         return true;
      } else {
         source.setCount(1);
         int maximum = source.getMaxDamage();
         int damage = MTConfig.PROJECTION_DURABILITY_FACTOR;
         if (MTConfig.PROJECTION_DURABILITY_FLAT) {
            damage = maximum - damage;
         }

         source.setDamageValue(Math.max(0, Math.min(damage, maximum - 1)));
         ItemStack projection = ProjectionSpellEffect.removeDisallowedEnchants(source);
         int strengthening = StrengtheningSpellEffect.getStrengthened(projection);
         if (strengthening > 0) {
            StrengtheningSpellEffect.reduceStrengthened(projection, strengthening);
         }

         ProjectionSpellEffect.setProjected(projection);
         if (!player.getInventory().add(projection)) {
            player.drop(projection, false);
         }

         player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.6F);
         player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_created", new Object[]{projection.getHoverName()}), true);
         return true;
      }
   }

   public static ItemStack getRememberedStack(ItemStack scroll) {
      return scroll != null && !scroll.isEmpty() ? new ComponentItemHandler(scroll, DataComponents.CONTAINER, 1).getStackInSlot(0) : ItemStack.EMPTY;
   }

   public static boolean clearMemory(ServerPlayer player, ItemStack scroll) {
      ItemStack remembered = getRememberedStack(scroll);
      if (remembered.isEmpty()) {
         player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_memory_empty"), true);
         return false;
      } else {
         new ComponentItemHandler(scroll, DataComponents.CONTAINER, 1).setStackInSlot(0, ItemStack.EMPTY);
         player.displayClientMessage(
            Component.translatable("message.mahoudim.unlimited_projection_memory_cleared", new Object[]{remembered.getHoverName()}), true
         );
         player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 0.7F);
         return true;
      }
   }

   public static ItemStack findTargetStack(Player player) {
      Entity target = Utils.selectEntityNearCursor(
         player,
         MTConfig.GLOBAL_LOOK_RANGE,
         player.level(),
         entity -> !firstProjectableStack(entity, player.level()).isEmpty(),
         Utils.nearAllowed(MAHOU_SELECTION.SCROLL_PROJECTION)
      );
      return target == null ? ItemStack.EMPTY : firstProjectableStack(target, player.level());
   }

   private static ItemStack firstProjectableStack(Entity entity, Level level) {
      if (entity instanceof LivingEntity living) {
         for (ItemStack held : living.getHandSlots()) {
            if (isProjectable(held, level)) {
               return held;
            }
         }
      }

      if (entity instanceof ItemEntity itemEntity && isProjectable(itemEntity.getItem(), level)) {
         return itemEntity.getItem();
      } else if (entity instanceof ItemFrame frame && isProjectable(frame.getItem(), level)) {
         return frame.getItem();
      } else if (entity instanceof WeaponProjectileEntity weapon && isProjectable(weapon.getStack(), level)) {
         return weapon.getStack();
      } else {
         return entity instanceof ThrownTrident trident && isProjectable(trident.getWeaponItem(), level) ? trident.getWeaponItem() : ItemStack.EMPTY;
      }
   }

   private static boolean isProjectable(ItemStack stack, Level level) {
      return stack != null
            && !stack.isEmpty()
            && !stack.has(DataComponents.CONTAINER)
            && !stack.has(DataComponents.BUNDLE_CONTENTS)
            && !stack.has(DataComponents.BLOCK_ENTITY_DATA)
            && !ProjectionSpellEffect.getProjected(stack)
         ? !MTConfig.PROJECTION_IS_BLACKLIST || !ProjectionSpellEffect.disallowed(stack.getItem(), level)
         : false;
   }

   public record CastPlan(boolean projection, ItemStack item) {
      public CastPlan(boolean projection, ItemStack item) {
         item = item.copy();
         item.setCount(1);
         this.projection = projection;
         this.item = item;
      }
   }
}

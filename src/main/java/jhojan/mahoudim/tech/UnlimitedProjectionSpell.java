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
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ComponentItemHandler;
import stepsword.mahoutsukai.config.MTConfig;
import stepsword.mahoutsukai.effects.projection.ProjectionSpellEffect;
import stepsword.mahoutsukai.effects.projection.StrengtheningSpellEffect;
import stepsword.mahoutsukai.entity.WeaponProjectileEntity;
import stepsword.mahoutsukai.util.Utils;

/**
 * A relaxed version of Mahou Tsukai's Projection: it removes the vanilla item-class
 * and explicit-whitelist checks, so damageable modded weapons/tools can be copied.
 * The base spell's durability cost, enchantment exclusions, projected marker and
 * server blacklist remain in effect.
 */
public final class UnlimitedProjectionSpell {
   private static final int MEMORY_SLOTS = 1;

   private UnlimitedProjectionSpell() {
   }

   public static boolean cast(ServerPlayer player, ItemStack scroll) {
      ComponentItemHandler memory = new ComponentItemHandler(scroll, DataComponents.CONTAINER, MEMORY_SLOTS);
      ItemStack remembered = memory.getStackInSlot(0);
      if (remembered.isEmpty()) {
         ItemStack target = findTargetStack(player);
         if (target.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_no_target"), true);
            return false;
         }
         ItemStack stored = target.copy();
         stored.setCount(1);
         memory.setStackInSlot(0, stored);
         player.displayClientMessage(
            Component.translatable("message.mahoudim.unlimited_projection_memorized", stored.getHoverName()), true
         );
         player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9F, 1.2F);
         return true;
      }

      if (!isProjectable(remembered, player.level())) {
         memory.setStackInSlot(0, ItemStack.EMPTY);
         player.displayClientMessage(Component.translatable("message.mahoudim.unlimited_projection_memory_invalid"), true);
         return false;
      }

      ItemStack projection = remembered.copy();
      projection.setCount(1);
      int maximum = projection.getMaxDamage();
      int damage = MTConfig.PROJECTION_DURABILITY_FACTOR;
      if (MTConfig.PROJECTION_DURABILITY_FLAT) {
         damage = maximum - damage;
      }
      projection.setDamageValue(Math.max(0, Math.min(damage, maximum - 1)));
      projection = ProjectionSpellEffect.removeDisallowedEnchants(projection);
      int strengthening = StrengtheningSpellEffect.getStrengthened(projection);
      if (strengthening > 0) {
         StrengtheningSpellEffect.reduceStrengthened(projection, strengthening);
      }
      ProjectionSpellEffect.setProjected(projection);

      if (!player.getInventory().add(projection)) {
         player.drop(projection, false);
      }
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.6F);
      player.displayClientMessage(
         Component.translatable("message.mahoudim.unlimited_projection_created", projection.getHoverName()), true
      );
      return true;
   }

   private static ItemStack findTargetStack(ServerPlayer player) {
      Entity target = Utils.selectEntityNearCursor(
         player,
         (double)MTConfig.GLOBAL_LOOK_RANGE,
         player.level(),
         entity -> !firstProjectableStack(entity, player.level()).isEmpty(),
         Utils.nearAllowed(Utils.MAHOU_SELECTION.SCROLL_PROJECTION)
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
      }
      if (entity instanceof ItemFrame frame && isProjectable(frame.getItem(), level)) {
         return frame.getItem();
      }
      if (entity instanceof WeaponProjectileEntity weapon && isProjectable(weapon.getStack(), level)) {
         return weapon.getStack();
      }
      if (entity instanceof ThrownTrident trident && isProjectable(trident.getWeaponItem(), level)) {
         return trident.getWeaponItem();
      }
      return ItemStack.EMPTY;
   }

   private static boolean isProjectable(ItemStack stack, Level level) {
      if (stack == null || stack.isEmpty() || !stack.isDamageableItem() || stack.getMaxDamage() < 1
         || stack.has(DataComponents.CONTAINER) || ProjectionSpellEffect.getProjected(stack)) {
         return false;
      }
      // Respect explicit server bans, but intentionally do not require the original
      // spell's vanilla-class whitelist or its optional allowlist.
      return !MTConfig.PROJECTION_IS_BLACKLIST || !ProjectionSpellEffect.disallowed(stack.getItem(), level);
   }
}

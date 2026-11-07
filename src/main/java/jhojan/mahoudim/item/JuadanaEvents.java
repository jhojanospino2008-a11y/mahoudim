package jhojan.mahoudim.item;

import jhojan.mahoudim.Reg;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.server.level.ServerPlayer;

public final class JuadanaEvents {
   private JuadanaEvents() {
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer killer) {
         ItemStack scythe = killer.getMainHandItem();
         if (scythe.is(Reg.JUADANA.get())) {
            JuadanaItem.setCharge(scythe, JuadanaItem.getCharge(scythe) + 1);
         }
      }
   }

   @SubscribeEvent
   public static void onScytheDamage(LivingDamageEvent.Pre event) {
      if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
         ItemStack scythe = attacker.getMainHandItem();
         if (scythe.is(Reg.JUADANA.get()) && JuadanaItem.getCharge(scythe) >= JuadanaItem.MAX_CHARGE) {
            event.setNewDamage(100.0F);
            JuadanaItem.setCharge(scythe, 0);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      Player player = event.getEntity();
      if (!(player instanceof ServerPlayer serverPlayer) || player.tickCount % 10 != 0) {
         return;
      }
      ItemStack scythe = player.getMainHandItem();
      if (!scythe.is(Reg.JUADANA.get())) {
         return;
      }

      int charge = JuadanaItem.getCharge(scythe);
      int filled = Math.min(20, charge / 5);
      MutableComponent bar = Component.literal("■".repeat(filled)).withStyle(ChatFormatting.DARK_RED)
         .append(Component.literal("□".repeat(20 - filled)).withStyle(ChatFormatting.DARK_GRAY));
      MutableComponent status = Component.literal("☠ ").withStyle(ChatFormatting.GRAY)
         .append(bar)
         .append(Component.literal("  " + charge + "/100").withStyle(charge >= 100 ? ChatFormatting.RED : ChatFormatting.WHITE));
      if (charge >= 100) {
         status.append(Component.literal("  ").append(Component.translatable("hud.mahoudim.juadana.ready")).withStyle(ChatFormatting.GOLD));
      }
      serverPlayer.displayClientMessage(status, true);
   }
}

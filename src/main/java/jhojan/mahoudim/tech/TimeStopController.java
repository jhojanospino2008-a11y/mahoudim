package jhojan.mahoudim.tech;

import java.util.UUID;
import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import stepsword.mahoutsukai.mana.PlayerManaManager;

public final class TimeStopController {
   private static final long NANOS_PER_SECOND = 1000000000L;
   private static MinecraftServer activeServer;
   private static UUID owner;
   private static long expiresAtNanos;
   private static long nextChargeAtNanos;
   private static int manaPerSecond;

   private TimeStopController() {
   }

   public static boolean start(ServerPlayer caster, int requestedManaPerSecond, int requestedSeconds) {
      MinecraftServer server = caster.server;
      if (activeServer != null) {
         caster.displayClientMessage(Component.translatable("mahoudim.msg.time_stop_busy"), true);
         return false;
      } else {
         ServerTickRateManager tickRateManager = server.tickRateManager();
         if (tickRateManager.isFrozen()) {
            caster.displayClientMessage(Component.translatable("mahoudim.msg.time_stop_server_frozen"), true);
            return false;
         } else {
            int rate = Math.max(1, requestedManaPerSecond);
            if (SecondSorceryCode.isBeingUsed(caster)) {
               rate = Math.max(1, (rate + 1) / 2);
            }

            int seconds = Math.max(1, Math.min(60, requestedSeconds));
            if (PlayerManaManager.drainMana(caster, rate, false, false) != rate) {
               caster.displayClientMessage(Component.translatable("mahoudim.msg.time_stop_need_mana", new Object[]{rate}), true);
               return false;
            } else {
               tickRateManager.stopSprinting();
               tickRateManager.stopStepping();
               tickRateManager.setFrozen(true);
               long now = System.nanoTime();
               activeServer = server;
               owner = caster.getUUID();
               manaPerSecond = rate;
               expiresAtNanos = now + seconds * 1000000000L;
               nextChargeAtNanos = now + 1000000000L;
               caster.displayClientMessage(Component.translatable("mahoudim.msg.time_stop_started", new Object[]{seconds, rate}), true);
               return true;
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      if (activeServer == server) {
         if (!server.tickRateManager().isFrozen()) {
            finish(server, "mahoudim.msg.time_stop_interrupted", false);
         } else {
            long now = System.nanoTime();
            if (now >= expiresAtNanos) {
               finish(server, "mahoudim.msg.time_stop_ended", true);
            } else {
               ServerPlayer caster = server.getPlayerList().getPlayer(owner);
               if (caster != null && caster.isAlive()) {
                  while (now >= nextChargeAtNanos && nextChargeAtNanos < expiresAtNanos) {
                     if (PlayerManaManager.drainMana(caster, manaPerSecond, false, false) != manaPerSecond) {
                        caster.displayClientMessage(Component.translatable("mahoudim.msg.time_stop_no_mana"), true);
                        finish(server, null, true);
                        return;
                     }

                     nextChargeAtNanos += 1000000000L;
                  }
               } else {
                  finish(server, "mahoudim.msg.time_stop_ended", true);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (owner != null && owner.equals(event.getEntity().getUUID()) && activeServer != null) {
         finish(activeServer, "mahoudim.msg.time_stop_ended", true);
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      if (activeServer == event.getServer()) {
         finish(event.getServer(), null, true);
      }
   }

   private static void finish(MinecraftServer server, String messageKey, boolean resume) {
      UUID previousOwner = owner;
      boolean wasActive = activeServer == server;
      if (wasActive) {
         activeServer = null;
         owner = null;
         expiresAtNanos = 0L;
         nextChargeAtNanos = 0L;
         manaPerSecond = 0;
         if (resume && server.tickRateManager().isFrozen()) {
            server.tickRateManager().setFrozen(false);
         }

         if (messageKey != null && previousOwner != null) {
            ServerPlayer caster = server.getPlayerList().getPlayer(previousOwner);
            if (caster != null) {
               caster.displayClientMessage(Component.translatable(messageKey), true);
            }
         }
      }
   }
}

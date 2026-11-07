package jhojan.mahoudim.tech;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import jhojan.mahoudim.Reg;

/** Server-side adaptation state for players carrying the Mahora wheel. */
public final class MahoraAdaptationEvents {
   private static final String ROOT_TAG = "mahoudim";
   private static final String ADAPTATION_TAG = "mahora_adaptation";
   private static final long MEMORY_TTL_TICKS = 5L * 60L * 20L;
   private static final int MAX_TRACKED_DAMAGE_TYPES = 16;
   private static final int MAX_HITS = 6;
   private static final int MAX_LEVEL = 3;

   private MahoraAdaptationEvents() {
   }

   @SubscribeEvent
   public static void onLivingDamage(LivingDamageEvent.Pre event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || !hasWheel(player)) {
         return;
      }

      float damage = event.getNewDamage();
      if (!(damage > 0.0F) || !Float.isFinite(damage)) {
         return;
      }

      String damageType = event.getSource().getMsgId();
      if (damageType == null || damageType.isBlank()) {
         damageType = "unknown";
      }

      CompoundTag playerData = player.getPersistentData();
      CompoundTag modData = playerData.getCompound(ROOT_TAG);
      CompoundTag adaptations = modData.getCompound(ADAPTATION_TAG);
      if (!adaptations.contains(damageType) && adaptations.size() >= MAX_TRACKED_DAMAGE_TYPES) {
         evictOldest(adaptations);
      }

      CompoundTag exposure = adaptations.getCompound(damageType);
      long now = player.server.overworld().getGameTime();
      int hits = exposure.getInt("hits");
      if (exposure.contains("last") && now - exposure.getLong("last") > MEMORY_TTL_TICKS) {
         hits = 0;
      }

      hits = Math.min(MAX_HITS, hits + 1);
      exposure.putInt("hits", hits);
      exposure.putLong("last", now);
      adaptations.put(damageType, exposure);
      modData.put(ADAPTATION_TAG, adaptations);
      playerData.put(ROOT_TAG, modData);

      int level = Math.min(MAX_LEVEL, hits / 2);
      if (level > 0) {
         event.setNewDamage(damage * (1.0F - 0.25F * level));
      }

      if (hits == 2 || hits == 4 || hits == 6) {
         ServerLevel levelWorld = player.serverLevel();
         Vec3 pos = player.position().add(0.0, player.getBbHeight() * 0.7, 0.0);
         levelWorld.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.8F + 0.2F * level);
         levelWorld.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 8, 0.25, 0.2, 0.25, 0.015);
         player.displayClientMessage(Component.translatable("mahoudim.msg.mahora_adapted", level), true);
      }
   }

   public static boolean hasWheel(ServerPlayer player) {
      var wheel = Reg.MAHORA_WHEEL.get();
      if (player.getMainHandItem().is(wheel) || player.getOffhandItem().is(wheel)) {
         return true;
      }

      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (stack.is(wheel)) {
            return true;
         }
      }
      return false;
   }

   private static void evictOldest(CompoundTag adaptations) {
      String oldestType = null;
      long oldestTime = Long.MAX_VALUE;
      for (String type : adaptations.getAllKeys()) {
         long last = adaptations.getCompound(type).getLong("last");
         if (last < oldestTime) {
            oldestTime = last;
            oldestType = type;
         }
      }
      if (oldestType != null) {
         adaptations.remove(oldestType);
      }
   }
}

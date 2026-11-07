package jhojan.mahoudim.tech;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import jhojan.mahoudim.Reg;

/** Server-side adaptation state for players carrying the Mahora wheel. */
public final class MahoraAdaptationEvents {
   private static final String ROOT_TAG = "mahoudim";
   private static final String ADAPTATION_TAG = "mahora_adaptation";
   private static final long MEMORY_TTL_TICKS = 5L * 60L * 20L;
   private static final int START_HITS = 3;
   private static final int HITS_PER_LEVEL = 2;
   private static final int MAX_LEVEL = 4;
   private static final float REDUCTION_PER_LEVEL = 0.20F;

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
      final String adaptedDamageType = damageType;

      CompoundTag playerData = player.getPersistentData();
      CompoundTag modData = playerData.getCompound(ROOT_TAG);
      CompoundTag adaptations = modData.getCompound(ADAPTATION_TAG);
      CompoundTag exposure = adaptations.getCompound(damageType);
      long now = player.server.overworld().getGameTime();
      int hits = exposure.getInt("hits");
      if (exposure.contains("last") && now - exposure.getLong("last") > MEMORY_TTL_TICKS) {
         hits = 0;
      }

      // There is deliberately no global type limit: Mahora can remember every
      // damage source independently. Each source forgets itself after five minutes.
      hits++;
      exposure.putInt("hits", hits);
      exposure.putLong("last", now);
      adaptations.put(damageType, exposure);
      modData.put(ADAPTATION_TAG, adaptations);
      playerData.put(ROOT_TAG, modData);

      int level = adaptationLevel(hits);
      if (level > 0) {
         event.setNewDamage(damage * (1.0F - REDUCTION_PER_LEVEL * level));
      }

      ItemStack wheel = player.getItemBySlot(EquipmentSlot.HEAD);
      if (level > 0 && (hits == START_HITS || (hits > START_HITS && (hits - START_HITS) % HITS_PER_LEVEL == 0))) {
         CustomData.update(DataComponents.CUSTOM_DATA, wheel, tag -> {
            ListTag types = tag.contains("mahora_adapted_types", Tag.TAG_LIST)
                  ? tag.getList("mahora_adapted_types", Tag.TAG_STRING) : new ListTag();
            boolean alreadyRecorded = false;
            for (int i = 0; i < types.size(); i++) {
               if (types.getString(i).equals(adaptedDamageType)) {
                  alreadyRecorded = true;
                  break;
               }
            }
            if (!alreadyRecorded) {
               types.add(StringTag.valueOf(adaptedDamageType));
               tag.put("mahora_adapted_types", types);
            }
         });
         CustomData.update(DataComponents.CUSTOM_DATA, wheel, tag ->
               tag.putInt("mahora_rotation", tag.getInt("mahora_rotation") + 1));
         ServerLevel levelWorld = player.serverLevel();
         Vec3 pos = player.position().add(0.0, player.getBbHeight() * 0.7, 0.0);
         levelWorld.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.8F + 0.2F * level);
         levelWorld.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 8, 0.25, 0.2, 0.25, 0.015);
         player.displayClientMessage(Component.translatable("mahoudim.msg.mahora_adapted", level), true);
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || !hasWheel(player)) {
         return;
      }

      ItemStack wheel = player.getItemBySlot(EquipmentSlot.HEAD);
      CompoundTag wheelData = wheel.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
      if (wheelData.contains("mahora_adapted_types", Tag.TAG_LIST)
            && !wheelData.getList("mahora_adapted_types", Tag.TAG_STRING).isEmpty()) {
         // Strength I while at least one damage type is adapted.
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
      }
   }

   private static int adaptationLevel(int hits) {
      if (hits < START_HITS) {
         return 0;
      }
      return Math.min(MAX_LEVEL, 1 + (hits - START_HITS) / HITS_PER_LEVEL);
   }

   public static boolean hasWheel(ServerPlayer player) {
      var wheel = Reg.MAHORA_WHEEL.get();
      return player.getItemBySlot(EquipmentSlot.HEAD).is(wheel);
   }
}

package jhojan.mahoudim.tech;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.item.MahoraWheelItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public final class MahoraAdaptationEvents {
   private static final String ROOT_TAG = "mahoudim";
   private static final String ADAPTATION_TAG = "mahora_adaptation";
   private static final long MEMORY_TTL_TICKS = 6000L;
   private static final int START_HITS = 3;
   private static final int HITS_PER_LEVEL = 2;
   private static final int MAX_LEVEL = 4;
   private static final float REDUCTION_PER_LEVEL = 0.2F;

   private MahoraAdaptationEvents() {
   }

   @SubscribeEvent
   public static void onLivingDamage(Pre event) {
      if (event.getEntity() instanceof ServerPlayer player && hasWheel(player)) {
         float damage = event.getNewDamage();
         if (damage > 0.0F && Float.isFinite(damage)) {
            String damageType = event.getSource().getMsgId();
            if (damageType == null || damageType.isBlank()) {
               damageType = "unknown";
            }

            String adaptedDamageType = damageType;
            CompoundTag playerData = player.getPersistentData();
            CompoundTag modData = playerData.getCompound("mahoudim");
            CompoundTag adaptations = modData.getCompound("mahora_adaptation");
            CompoundTag exposure = adaptations.getCompound(damageType);
            long now = player.server.overworld().getGameTime();
            int hits = exposure.getInt("hits");
            if (exposure.contains("last") && now - exposure.getLong("last") > 6000L) {
               hits = 0;
            }

            exposure.putInt("hits", ++hits);
            exposure.putLong("last", now);
            adaptations.put(damageType, exposure);
            modData.put("mahora_adaptation", adaptations);
            playerData.put("mahoudim", modData);
            int level = adaptationLevel(hits);
            if (level > 0) {
               event.setNewDamage(damage * (1.0F - 0.2F * level));
            }

            ItemStack wheel = player.getItemBySlot(EquipmentSlot.HEAD);
            if (level > 0 && (hits == 3 || hits > 3 && (hits - 3) % 2 == 0)) {
               CustomData.update(DataComponents.CUSTOM_DATA, wheel, tag -> {
                  ListTag types = tag.contains("mahora_adapted_types", 9) ? tag.getList("mahora_adapted_types", 8) : new ListTag();
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
               CustomData.update(DataComponents.CUSTOM_DATA, wheel, tag -> tag.putInt("mahora_rotation", tag.getInt("mahora_rotation") + 1));
               ServerLevel levelWorld = player.serverLevel();
               Vec3 pos = player.position().add(0.0, player.getBbHeight() * 0.7, 0.0);
               levelWorld.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8F, 0.8F + 0.2F * level);
               levelWorld.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 8, 0.25, 0.2, 0.25, 0.015);
               player.displayClientMessage(Component.translatable("mahoudim.msg.mahora_adapted", new Object[]{level}), true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player && hasWheel(player)) {
         ItemStack wheel = player.getItemBySlot(EquipmentSlot.HEAD);
         CompoundTag wheelData = ((CustomData)wheel.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
         if (wheelData.contains("mahora_adapted_types", 9) && !wheelData.getList("mahora_adapted_types", 8).isEmpty()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false));
         }
      }
   }

   private static int adaptationLevel(int hits) {
      return hits < 3 ? 0 : Math.min(4, 1 + (hits - 3) / 2);
   }

   public static boolean hasWheel(ServerPlayer player) {
      MahoraWheelItem wheel = (MahoraWheelItem)Reg.MAHORA_WHEEL.get();
      return player.getItemBySlot(EquipmentSlot.HEAD).is(wheel);
   }
}

package jhojan.mahoudim.tech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import jhojan.mahoudim.Reg;
import jhojan.mahoudim.item.EffectInscriptionBook;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class EffectInscriptionEvents {
   private static final String INSCRIBED_MARKER = "mahoudim_effect_inscribed";
   private static final Map<UUID, Map<ResourceLocation, HeldEffect>> ACTIVE_EFFECTS = new HashMap<>();

   private EffectInscriptionEvents() {
   }

   @SubscribeEvent
   public static void onAnvilUpdate(AnvilUpdateEvent event) {
      ItemStack book = event.getRight();
      ItemStack target = event.getLeft();
      if (!book.is(Reg.EFFECT_INSCRIPTION_BOOK.get()) || target.isEmpty() || target.getItem() instanceof EffectInscriptionBook
         || (!target.isDamageableItem() && target.get(DataComponents.TOOL) == null)) {
         return;
      }

      PotionContents bookContents = book.get(DataComponents.POTION_CONTENTS);
      if (bookContents == null || bookContents.customEffects().isEmpty()) {
         return;
      }

      List<MobEffectInstance> merged = new ArrayList<>();
      if (isInscribed(target)) {
         PotionContents existing = target.get(DataComponents.POTION_CONTENTS);
         if (existing != null) {
            existing.customEffects().forEach(effect -> merged.add(new MobEffectInstance(effect)));
         }
      }
      for (MobEffectInstance added : bookContents.customEffects()) {
         merged.removeIf(current -> current.getEffect().equals(added.getEffect()));
         merged.add(new MobEffectInstance(added));
      }
      if (merged.isEmpty() || merged.size() > 8) {
         return;
      }

      ItemStack output = target.copy();
      output.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), merged));
      CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean(INSCRIBED_MARKER, true));
      event.setOutput(output);
      event.setCost(3L);
      event.setMaterialCost(1);
   }

   @SubscribeEvent
   public static void onTooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (!isInscribed(stack)) {
         return;
      }
      event.getToolTip().add(Component.translatable("item.mahoudim.effect_inscribed.tooltip"));
      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return;
      }
      for (MobEffectInstance effect : contents.customEffects()) {
         Component name = Component.translatable(effect.getDescriptionId());
         event.getToolTip().add(Component.translatable("item.mahoudim.effect_inscribed.entry", name, effect.getAmplifier() + 1));
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      Map<ResourceLocation, MobEffectInstance> held = getHeldInscriptions(player);
      Map<ResourceLocation, HeldEffect> active = ACTIVE_EFFECTS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
      long now = player.serverLevel().getGameTime();

      for (Map.Entry<ResourceLocation, MobEffectInstance> entry : held.entrySet()) {
         Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(entry.getKey());
         if (holder.isEmpty()) {
            continue;
         }
         MobEffectInstance desired = entry.getValue();
         HeldEffect state = active.get(entry.getKey());
         MobEffectInstance current = player.getEffect(holder.get());
         if (state == null) {
            if (current != null && current.getAmplifier() > desired.getAmplifier()) {
               continue;
            }
            state = new HeldEffect(
               holder.get(), current == null ? null : new MobEffectInstance(current), now, desired.getAmplifier(),
               holder.get().value().isInstantenous(), desired.getDuration(), desired.isAmbient(), desired.isVisible(), desired.showIcon()
            );
            active.put(entry.getKey(), state);
            player.addEffect(new MobEffectInstance(desired));
         } else if (!state.matches(desired)) {
            finish(player, state, now);
            active.remove(entry.getKey());
            current = player.getEffect(holder.get());
            if (current == null || current.getAmplifier() <= desired.getAmplifier()) {
               state = new HeldEffect(
                  holder.get(), current == null ? null : new MobEffectInstance(current), now, desired.getAmplifier(),
                  holder.get().value().isInstantenous(), desired.getDuration(), desired.isAmbient(), desired.isVisible(), desired.showIcon()
               );
               active.put(entry.getKey(), state);
               player.addEffect(new MobEffectInstance(desired));
            }
         } else if (!state.instant) {
            current = player.getEffect(holder.get());
            if (current == null || current.getAmplifier() < state.appliedAmplifier || current.getDuration() <= 1) {
               player.addEffect(new MobEffectInstance(
                  holder.get(), Math.max(20, state.duration), state.appliedAmplifier, state.ambient, state.visible, state.showIcon
               ));
            }
         }
      }

      Set<ResourceLocation> noLongerHeld = new HashSet<>(active.keySet());
      noLongerHeld.removeAll(held.keySet());
      for (ResourceLocation id : noLongerHeld) {
         HeldEffect state = active.remove(id);
         if (state != null) {
            finish(player, state, now);
         }
      }
      if (active.isEmpty()) {
         ACTIVE_EFFECTS.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         Map<ResourceLocation, HeldEffect> active = ACTIVE_EFFECTS.remove(player.getUUID());
         if (active != null) {
            long now = player.serverLevel().getGameTime();
            active.values().forEach(state -> finish(player, state, now));
         }
      }
   }

   private static Map<ResourceLocation, MobEffectInstance> getHeldInscriptions(Player player) {
      Map<ResourceLocation, MobEffectInstance> effects = new HashMap<>();
      addHeldInscriptions(effects, player.getMainHandItem());
      addHeldInscriptions(effects, player.getOffhandItem());
      return effects;
   }

   private static void addHeldInscriptions(Map<ResourceLocation, MobEffectInstance> target, ItemStack stack) {
      if (!isInscribed(stack)) {
         return;
      }
      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return;
      }
      for (MobEffectInstance effect : contents.customEffects()) {
         ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
         MobEffectInstance previous = target.get(id);
         if (previous == null || previous.getAmplifier() < effect.getAmplifier()) {
            target.put(id, new MobEffectInstance(effect));
         }
      }
   }

   private static boolean isInscribed(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data != null && data.copyTag().getBoolean(INSCRIBED_MARKER);
   }

   private static void finish(ServerPlayer player, HeldEffect state, long now) {
      MobEffectInstance current = player.getEffect(state.holder);
      if (current != null && current.getAmplifier() > state.appliedAmplifier) {
         return;
      }
      player.removeEffect(state.holder);
      if (state.previous != null) {
         int duration = state.previous.getDuration();
         if (!state.previous.isInfiniteDuration()) {
            duration -= (int)Math.max(0L, now - state.startedAt);
         }
         if (duration > 0 || duration == MobEffectInstance.INFINITE_DURATION) {
            player.addEffect(new MobEffectInstance(
               state.holder, duration, state.previous.getAmplifier(), state.previous.isAmbient(), state.previous.isVisible(), state.previous.showIcon()
            ));
         }
      }
   }

   private static final class HeldEffect {
      private final Holder<MobEffect> holder;
      private final MobEffectInstance previous;
      private final long startedAt;
      private final int appliedAmplifier;
      private final boolean instant;
      private final int duration;
      private final boolean ambient;
      private final boolean visible;
      private final boolean showIcon;

      private HeldEffect(Holder<MobEffect> holder, MobEffectInstance previous, long startedAt, int appliedAmplifier, boolean instant,
         int duration, boolean ambient, boolean visible, boolean showIcon) {
         this.holder = holder;
         this.previous = previous;
         this.startedAt = startedAt;
         this.appliedAmplifier = appliedAmplifier;
         this.instant = instant;
         this.duration = duration;
         this.ambient = ambient;
         this.visible = visible;
         this.showIcon = showIcon;
      }

      private boolean matches(MobEffectInstance effect) {
         return this.appliedAmplifier == effect.getAmplifier() && this.duration == effect.getDuration()
            && this.ambient == effect.isAmbient() && this.visible == effect.isVisible() && this.showIcon == effect.showIcon();
      }
   }
}

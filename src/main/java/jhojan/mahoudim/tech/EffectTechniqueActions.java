package jhojan.mahoudim.tech;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jhojan.mahoudim.Reg;
import jhojan.mahoudim.item.SecondSorceryCode;
import jhojan.mahoudim.net.EffectActionPacket;
import jhojan.mahoudim.net.OpenEffectScreenPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.network.PacketDistributor;
import stepsword.mahoutsukai.mana.PlayerManaManager;

public final class EffectTechniqueActions {
   public static final int KIND_INCREMENT = 0;
   public static final int KIND_CREATE = 1;
   public static final int ACTION_INCREMENT = 0;
   public static final int ACTION_CREATE = 1;
   public static final int MODE_BOOK = 1;
   public static final int MODE_POTION = 2;
   public static final int MAX_LEVEL = 256;
   public static final int MAX_DURATION_SECONDS = 72000;

   private static final Map<UUID, Pending> PENDING = new HashMap<>();

   private EffectTechniqueActions() {
   }

   public static boolean canBegin(Technique technique, ServerPlayer player) {
      if (technique == Technique.EFFECT_INCREMENT) {
         if (!player.getActiveEffects().isEmpty()) {
            return true;
         }
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_increment_none"), true);
         return false;
      }
      if (technique == Technique.EFFECT_CREATE) {
         if (getOutputMode(player) != 0) {
            return true;
         }
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_create_catalyst"), true);
         return false;
      }
      return true;
   }

   public static boolean openIncrement(ServerPlayer player) {
      if (!canBegin(Technique.EFFECT_INCREMENT, player)) {
         return false;
      }
      PENDING.put(player.getUUID(), new Pending(KIND_INCREMENT, 0, player.serverLevel().getGameTime() + 1200L));
      PacketDistributor.sendToPlayer(player, new OpenEffectScreenPacket(KIND_INCREMENT, 0), new CustomPacketPayload[0]);
      return true;
   }

   public static boolean openCreate(ServerPlayer player) {
      int outputMode = getOutputMode(player);
      if (outputMode == 0) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_create_catalyst"), true);
         return false;
      }
      PENDING.put(player.getUUID(), new Pending(KIND_CREATE, outputMode, player.serverLevel().getGameTime() + 1200L));
      PacketDistributor.sendToPlayer(player, new OpenEffectScreenPacket(KIND_CREATE, outputMode), new CustomPacketPayload[0]);
      return true;
   }

   private static int getOutputMode(ServerPlayer player) {
      ItemStack offhand = player.getOffhandItem();
      if (offhand.is(Items.BOOK)) {
         return MODE_BOOK;
      }
      if (offhand.is(Items.POTION)) {
         PotionContents contents = offhand.get(DataComponents.POTION_CONTENTS);
         if (contents != null && contents.is(Potions.WATER)) {
            return MODE_POTION;
         }
      }
      return 0;
   }

   public static void handle(ServerPlayer player, EffectActionPacket packet) {
      Pending pending = PENDING.remove(player.getUUID());
      if (pending == null || pending.expiresAt() < player.serverLevel().getGameTime()
         || packet.action() != pending.kind() || packet.outputMode() != pending.outputMode()) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_menu_expired"), true);
         return;
      }

      Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(packet.effectId());
      if (holder.isEmpty()) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_unknown", packet.effectId().toString()), true);
         return;
      }
      if (packet.level() < 1 || packet.level() > MAX_LEVEL) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_invalid_level"), true);
         return;
      }

      if (pending.kind() == KIND_INCREMENT) {
         increment(player, packet, holder.get());
      } else {
         create(player, packet, holder.get(), pending.outputMode());
      }
   }

   private static void increment(ServerPlayer player, EffectActionPacket packet, Holder<MobEffect> effect) {
      MobEffectInstance current = player.getEffect(effect);
      if (current == null || packet.level() <= current.getAmplifier() + 1) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_increment_invalid"), true);
         return;
      }
      int targetLevel = SecondSorceryCode.isBeingUsed(player)
         ? Math.min(MAX_LEVEL, (int)Math.ceil(packet.level() * 1.5D)) : packet.level();
      int manaCost = Math.min(100000, targetLevel * 100);
      if (!charge(player, manaCost)) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_not_enough_mana", manaCost), true);
         return;
      }
      MobEffectInstance upgraded = new MobEffectInstance(
         effect, current.getDuration(), targetLevel - 1, current.isAmbient(), current.isVisible(), current.showIcon()
      );
      player.addEffect(upgraded);
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
      player.displayClientMessage(
         Component.translatable("mahoudim.msg.effect_increment_success", Component.translatable(effect.value().getDescriptionId()), targetLevel), true
      );
   }

   private static void create(ServerPlayer player, EffectActionPacket packet, Holder<MobEffect> effect, int outputMode) {
      if (packet.durationSeconds() < 1 || packet.durationSeconds() > MAX_DURATION_SECONDS) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_invalid_duration"), true);
         return;
      }
      if (getOutputMode(player) != outputMode) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_create_catalyst"), true);
         return;
      }

      boolean enhanced = SecondSorceryCode.isBeingUsed(player);
      int level = enhanced ? Math.min(MAX_LEVEL, (int)Math.ceil(packet.level() * 1.5D)) : packet.level();
      int durationSeconds = enhanced
         ? Math.min(MAX_DURATION_SECONDS, (int)Math.ceil(packet.durationSeconds() * 1.5D)) : packet.durationSeconds();
      int durationTicks = durationSeconds * 20;
      MobEffectInstance instance = new MobEffectInstance(effect, durationTicks, level - 1, false, packet.particles(), true);
      int manaCost = (int)Math.min(100000L, Math.max(25L, (long)durationSeconds * level));
      if (!charge(player, manaCost)) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_not_enough_mana", manaCost), true);
         return;
      }

      Component effectName = Component.translatable(effect.value().getDescriptionId());
      if (outputMode == MODE_BOOK) {
         ItemStack book = new ItemStack(Reg.EFFECT_INSCRIPTION_BOOK.get());
         book.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withEffectAdded(instance));
         book.set(DataComponents.CUSTOM_NAME, Component.translatable("item.mahoudim.effect_inscription_book.named", effectName));
         if (!player.isCreative()) {
            player.getOffhandItem().shrink(1);
         }
         if (!player.getInventory().add(book)) {
            player.drop(book, false);
         }
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_created_book", effectName, level), true);
      } else {
         ItemStack bottle = player.getOffhandItem();
         bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.of(Potions.WATER), Optional.empty(), List.of(instance)));
         bottle.set(DataComponents.CUSTOM_NAME, Component.translatable("item.mahoudim.effect_potion.named", effectName));
         player.displayClientMessage(Component.translatable("mahoudim.msg.effect_created_potion", effectName, level), true);
      }
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.9F, 1.1F);
   }

   private static boolean charge(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      }
      int cost = SecondSorceryCode.isBeingUsed(player) ? (amount + 1) / 2 : amount;
      return PlayerManaManager.drainMana(player, cost, false, false) == cost;
   }

   private record Pending(int kind, int outputMode, long expiresAt) {
   }
}

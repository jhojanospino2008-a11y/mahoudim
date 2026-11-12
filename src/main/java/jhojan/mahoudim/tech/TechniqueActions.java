package jhojan.mahoudim.tech;

import java.util.List;
import jhojan.mahoudim.Reg;
import jhojan.mahoudim.item.SecondSorceryCode;
import jhojan.mahoudim.net.OpenCorePacket;
import jhojan.mahoudim.net.OpenGatePacket;
import jhojan.mahoudim.realm.RealmData;
import jhojan.mahoudim.realm.RealmManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TechniqueActions {
   private TechniqueActions() {
   }

   private static boolean enhanced(ServerPlayer player) {
      return SecondSorceryCode.isBeingUsed(player);
   }

   private static int effect(ServerPlayer player, int value) {
      return enhanced(player) ? Math.max(1, (int)Math.ceil(value * 1.5D)) : value;
   }

   private static int amplifier(ServerPlayer player, int value) {
      return enhanced(player) ? Math.max(0, (int)Math.ceil((value + 1) * 1.5D) - 1) : value;
   }

   public static boolean cast(Technique t, ServerPlayer p) {
      ServerLevel level = p.serverLevel();

      return switch (t) {
         case TEMPORAL_REWIND -> rewind(p, level, effect(p, t.param(0, 10)));
         case TIME_STASIS -> stasis(p, level, effect(p, t.param(0, 12)), effect(p, t.param(1, 8)));
         case CHRONO_ACCELERATION -> accelerate(p, level, effect(p, t.param(0, 8)), effect(p, t.param(1, 30)));
         case TIME_DILATION -> dilate(p, level, effect(p, t.param(0, 16)), effect(p, t.param(1, 30)));
         case CHRONO_SHIFT -> shiftTime(p, level);
         case TEMPORAL_BUBBLE -> temporalBubble(p, level, effect(p, t.param(0, 12)), effect(p, t.param(1, 15)));
         case TIME_LEAP -> timeLeap(p, level, effect(p, t.param(0, 600)));
         case CHRONO_GARDEN -> chronoGarden(p, level, effect(p, t.param(0, 10)), effect(p, t.param(1, 20)));
         case TEMPORAL_REGENERATION -> temporalRegeneration(p, level, effect(p, t.param(0, 12)));
         case TIME_SHELTER -> timeShelter(p, level, effect(p, t.param(0, 10)), effect(p, t.param(1, 12)));
         case TEMPORAL_CORE -> openCore(p);
         case SPATIAL_STEP -> step(p, level, effect(p, t.param(0, 24)));
         case SPATIAL_ANCHOR -> recallAnchor(p);
         case SPATIAL_VACUUM -> vacuum(p, level, effect(p, t.param(0, 20)));
         case SPATIAL_TUNNEL -> tunnel(p, level, effect(p, t.param(0, 24)));
         case SPATIAL_SWAP -> spatialSwap(p, level, effect(p, t.param(0, 20)));
         case SPATIAL_BEACON -> RealmManager.returnHome(p);
         case SPATIAL_PULSE -> spatialPulse(p, level, effect(p, t.param(0, 12)));
         case SPATIAL_PHASE -> spatialPhase(p, level, effect(p, t.param(0, 10)));
         case VOID_STEP -> step(p, level, effect(p, t.param(0, 36)));
         case DIMENSION_TUNER -> openDimensionTuner(p, level);
         case MAHORA_ADAPTATION -> grantMahoraWheel(p, level);
         case UNLIMITED_PROJECTION -> false;
      };
   }

   private static void msg(ServerPlayer p, String key, Object... args) {
      p.displayClientMessage(Component.translatable(key, args), true);
   }

   private static boolean grantMahoraWheel(ServerPlayer p, ServerLevel level) {
      if (MahoraAdaptationEvents.hasWheel(p)) {
         msg(p, "mahoudim.msg.mahora_already_have_wheel");
         return false;
      }

      ItemStack wheel = new ItemStack(Reg.MAHORA_WHEEL.get());
      if (!p.getInventory().add(wheel)) {
         p.drop(wheel, false);
      }

      level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.7F);
      msg(p, "mahoudim.msg.mahora_wheel_received");
      return true;
   }

   private static boolean openCore(ServerPlayer p) {
      PacketDistributor.sendToPlayer(p, new OpenCorePacket(), new CustomPacketPayload[0]);
      return true;
   }

   private static boolean openDimensionTuner(ServerPlayer p, ServerLevel level) {
      PacketDistributor.sendToPlayer(
         p, new OpenGatePacket(p.blockPosition(), RealmData.config(p).toTag(), RealmManager.inRealm(level)), new CustomPacketPayload[0]
      );
      return true;
   }

   private static boolean rewind(ServerPlayer p, ServerLevel level, int secondsBack) {
      TechniqueEvents.Snapshot s = TechniqueEvents.snapshotBack(p, secondsBack);
      if (s == null) {
         msg(p, "mahoudim.msg.no_history");
         return false;
      } else {
         ServerLevel dest = p.server.getLevel(s.dimension());
         if (dest == null) {
            dest = level;
         }

         if (dest != level) {
            p.teleportTo(dest, s.x(), s.y(), s.z(), s.yaw(), s.pitch());
         } else {
            p.connection.teleport(s.x(), s.y(), s.z(), s.yaw(), s.pitch());
         }

         p.setHealth(Math.min(p.getMaxHealth(), Math.max(1.0F, s.health())));
         p.getFoodData().setFoodLevel(s.food());
         p.fallDistance = 0.0F;
         dest.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
         return true;
      }
   }

   private static boolean stasis(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      AABB box = p.getBoundingBox().inflate((double)radius);
      int count = 0;
      long until = level.getGameTime() + (long)seconds * 20L;

      for (Entity e : level.getEntities(p, box)) {
         if (e instanceof ServerPlayer other) {
            other.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20, amplifier(p, 6)));
            other.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, seconds * 20, amplifier(p, 3)));
            count++;
         } else if (e instanceof Mob || e instanceof Projectile) {
            TechniqueEvents.freeze(e, until);
            count++;
         }
      }

      level.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 0.6F);
      msg(p, "mahoudim.msg.stasis", count);
      return true;
   }

   private static boolean accelerate(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      TechniqueEvents.startAcceleration(p, level.getGameTime() + (long)seconds * 20L, radius);
      p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, seconds * 20, amplifier(p, 1)));
      p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, seconds * 20, amplifier(p, 1)));
      level.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
      return true;
   }

   private static boolean dilate(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      int n = 0;

      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate((double)radius))) {
         if (e != p && e instanceof Enemy) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20, amplifier(p, 2)));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20, amplifier(p, 1)));
            n++;
         }
      }

      p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, seconds * 20, amplifier(p, 0)));
      level.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.7F);
      msg(p, "mahoudim.msg.dilation", n);
      return true;
   }

   private static boolean shiftTime(ServerPlayer p, ServerLevel level) {
      if (level.dimensionType().hasFixedTime()) {
         msg(p, "mahoudim.msg.fixed_time");
         return false;
      } else {
         long t = level.getDayTime();
         long base = t - Math.floorMod(t, 24000L);
         long tod = Math.floorMod(t, 24000L);
         long next = tod < 12500L ? base + 13000L : base + 24000L;
         long difference = next - t;
         level.setDayTime(enhanced(p) ? t + (long)Math.ceil(difference * 1.5D) : next);
         level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.5F);
         return true;
      }
   }

   private static boolean temporalBubble(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      int n = 0;

      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate((double)radius))) {
         if (e != p && e instanceof Enemy) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20, amplifier(p, 4)));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20, amplifier(p, 1)));
            n++;
         }
      }

      p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, seconds * 20, amplifier(p, 1)));
      p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, seconds * 20, amplifier(p, 1)));
      level.playSound(null, p.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 1.3F);
      msg(p, "mahoudim.msg.temporal_bubble", n);
      return true;
   }

   private static boolean timeLeap(ServerPlayer p, ServerLevel level, int ticks) {
      if (level.dimensionType().hasFixedTime()) {
         msg(p, "mahoudim.msg.fixed_time");
         return false;
      } else {
         level.setDayTime(level.getDayTime() + (long)ticks);
         p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, effect(p, 100), amplifier(p, 1)));
         level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.5F);
         return true;
      }
   }

   private static boolean chronoGarden(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      BlockPos c = p.blockPosition();
      int passes = Math.max(1, seconds / 2);
      int ticks = 0;

      for (int pass = 0; pass < passes; pass++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  BlockPos pos = c.offset(dx, dy, dz);
                  BlockState state = level.getBlockState(pos);
                  if (state.isRandomlyTicking()) {
                     state.randomTick(level, pos, level.random);
                     ticks++;
                  }
               }
            }
         }
      }

      p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, seconds * 20, amplifier(p, 1)));
      level.playSound(null, c, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.3F, 1.8F);
      msg(p, "mahoudim.msg.chrono_garden", ticks);
      return ticks > 0;
   }

   public static boolean activateChronoGarden(ServerPlayer p, int radius, int seconds, int multiplier) {
      return chronoGarden(p, p.serverLevel(), radius, seconds * multiplier);
   }

   private static boolean temporalRegeneration(ServerPlayer p, ServerLevel level, int seconds) {
      p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, seconds * 20, amplifier(p, 2)));
      p.addEffect(new MobEffectInstance(MobEffects.SATURATION, effect(p, 40), amplifier(p, 1)));
      p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, seconds * 20, amplifier(p, 1)));
      level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
      return true;
   }

   private static boolean timeShelter(ServerPlayer p, ServerLevel level, int radius, int seconds) {
      long until = level.getGameTime() + (long)seconds * 20L;
      int count = 0;

      for (Mob mob : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate((double)radius))) {
         TechniqueEvents.freeze(mob, until);
         count++;
      }

      p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, seconds * 20, amplifier(p, 1)));
      msg(p, "mahoudim.msg.time_shelter", count);
      return true;
   }

   private static boolean step(ServerPlayer p, ServerLevel level, int dist) {
      Vec3 look = p.getLookAngle();
      Vec3 start = p.position();
      Vec3 best = null;

      for (double d = 0.5; d <= (double)dist; d += 0.5) {
         Vec3 pos = start.add(look.scale(d));
         AABB box = p.getBoundingBox().move(pos.subtract(start));
         if (level.noCollision(p, box)) {
            best = pos;
         } else if (best != null) {
            break;
         }
      }

      if (best == null) {
         msg(p, "mahoudim.msg.blocked");
         return false;
      } else {
         p.connection.teleport(best.x, best.y, best.z, p.getYRot(), p.getXRot());
         p.fallDistance = 0.0F;
         level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
         return true;
      }
   }

   public static void markAnchor(ServerPlayer p) {
      CompoundTag t = new CompoundTag();
      t.putString("dim", p.level().dimension().location().toString());
      t.putDouble("x", p.getX());
      t.putDouble("y", p.getY());
      t.putDouble("z", p.getZ());
      t.putFloat("yaw", p.getYRot());
      t.putFloat("pitch", p.getXRot());
      p.getPersistentData().put("mahoudim_anchor", t);
      msg(p, "mahoudim.msg.anchor_marked");
      p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   private static boolean recallAnchor(ServerPlayer p) {
      CompoundTag t = p.getPersistentData().getCompound("mahoudim_anchor");
      ResourceLocation id = ResourceLocation.tryParse(t.getString("dim"));
      if (!t.isEmpty() && id != null) {
         ServerLevel dest = p.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
         if (dest == null) {
            msg(p, "mahoudim.msg.no_anchor");
            return false;
         } else {
            dest.getChunk((int)Math.floor(t.getDouble("x")) >> 4, (int)Math.floor(t.getDouble("z")) >> 4);
            p.teleportTo(dest, t.getDouble("x"), t.getDouble("y"), t.getDouble("z"), t.getFloat("yaw"), t.getFloat("pitch"));
            p.fallDistance = 0.0F;
            dest.playSound(null, p.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.4F, 1.4F);
            return true;
         }
      } else {
         msg(p, "mahoudim.msg.no_anchor");
         return false;
      }
   }

   private static boolean vacuum(ServerPlayer p, ServerLevel level, int radius) {
      AABB box = p.getBoundingBox().inflate((double)radius);
      int n = 0;

      for (ItemEntity it : level.getEntitiesOfClass(ItemEntity.class, box)) {
         it.setPos(p.getX(), p.getY() + 0.3, p.getZ());
         it.setNoPickUpDelay();
         n++;
      }

      for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
         orb.setPos(p.getX(), p.getY() + 0.3, p.getZ());
         n++;
      }

      level.playSound(null, p.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
      msg(p, "mahoudim.msg.vacuum", n);
      return true;
   }

   private static boolean tunnel(ServerPlayer p, ServerLevel level, int length) {
      Vec3 look = p.getLookAngle();
      Direction dir = Direction.getNearest(look.x, look.y, look.z);
      BlockPos feet = p.blockPosition();
      int broken = 0;

      for (int i = 1; i <= length; i++) {
         for (int a = -1; a <= 1; a++) {
            for (int b = 0; b <= 2; b++) {
               BlockPos pos;
               if (dir.getAxis().isVertical()) {
                  pos = feet.offset(a, 0, b - 1).relative(dir, i);
               } else {
                  Direction side = dir.getClockWise();
                  pos = feet.relative(dir, i).relative(side, a).above(b);
               }

               if (breakInto(p, level, pos)) {
                  broken++;
               }
            }
         }
      }

      level.playSound(null, p.blockPosition(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
      msg(p, "mahoudim.msg.tunnel", broken);
      return broken > 0;
   }

   private static boolean spatialSwap(ServerPlayer p, ServerLevel level, int radius) {
      LivingEntity target = null;
      double best = Double.MAX_VALUE;

      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate((double)radius))) {
         if (e != p && e instanceof Enemy && e.distanceToSqr(p) < best) {
            target = e;
            best = e.distanceToSqr(p);
         }
      }

      if (target == null) {
         msg(p, "mahoudim.msg.no_swap_target");
         return false;
      } else {
         Vec3 old = p.position();
         p.connection.teleport(target.getX(), target.getY(), target.getZ(), p.getYRot(), p.getXRot());
         target.teleportTo(old.x, old.y, old.z);
         level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 0.8F);
         return true;
      }
   }

   private static boolean spatialPulse(ServerPlayer p, ServerLevel level, int radius) {
      int moved = 0;

      for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate((double)radius))) {
         if (e != p && e instanceof Enemy) {
            Vec3 away = e.position().subtract(p.position()).normalize();
            if (away.lengthSqr() < 0.1) {
               away = new Vec3(1.0, 0.0, 0.0);
            }

            double displacement = effect(p, 8);
            e.teleportTo(e.getX() + away.x * displacement, e.getY() + 1.0, e.getZ() + away.z * displacement);
            moved++;
         }
      }

      level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 1.6F);
      msg(p, "mahoudim.msg.spatial_pulse", moved);
      return moved > 0;
   }

   private static boolean spatialPhase(ServerPlayer p, ServerLevel level, int seconds) {
      p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, seconds * 20, amplifier(p, 0)));
      p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, seconds * 20, amplifier(p, 0)));
      p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, seconds * 20, amplifier(p, 1)));
      level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);
      return true;
   }

   private static boolean breakInto(ServerPlayer p, ServerLevel level, BlockPos pos) {
      BlockState st = level.getBlockState(pos);
      if (!st.isAir() && !(st.getDestroySpeed(level, pos) < 0.0F)) {
         if (!st.getFluidState().isEmpty()) {
            return false;
         } else if (level.getBlockEntity(pos) != null) {
            return false;
         } else {
            BreakEvent ev = new BreakEvent(level, pos, st, p);
            NeoForge.EVENT_BUS.post(ev);
            if (ev.isCanceled()) {
               return false;
            } else {
               List<ItemStack> drops = Block.getDrops(st, level, pos, null, p, p.getMainHandItem());
               level.removeBlock(pos, false);

               for (ItemStack s : drops) {
                  if (!p.getInventory().add(s) && !s.isEmpty()) {
                     Block.popResource(level, p.blockPosition(), s);
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }
}

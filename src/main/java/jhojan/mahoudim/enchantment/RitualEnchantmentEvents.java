package jhojan.mahoudim.enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

public final class RitualEnchantmentEvents {
   private static final ResourceLocation CADENCE_MODIFIER = ResourceLocation.fromNamespaceAndPath("mahoudim", "cadence_slow");
   private static final Map<UUID, RitualEnchantmentEvents.HitChain> HIT_CHAINS = new HashMap<>();
   private static final Map<UUID, Long> CADENCE_UNTIL = new HashMap<>();
   private static final Map<UUID, UUID> LAST_TARGET = new HashMap<>();
   private static final Map<UUID, Long> INERTIA_READY = new HashMap<>();
   private static final Map<UUID, Integer> INERTIA_LEVELS = new HashMap<>();
   private static final Map<UUID, Integer> INERTIA_READY_LEVEL = new HashMap<>();
   private static final Map<UUID, Long> GUARD_READY = new HashMap<>();
   private static final Map<UUID, Long> GUARD_COOLDOWN = new HashMap<>();
   private static final Map<UUID, RitualEnchantmentEvents.BlockedHit> BLOCKED_HITS = new HashMap<>();
   private static final Map<UUID, Long> WATER_TRANSITIONS = new HashMap<>();
   private static final Map<UUID, Boolean> WATER_STATE = new HashMap<>();
   private static final Map<UUID, Long> UMBRAL_COOLDOWN = new HashMap<>();
   private static final Map<UUID, List<RitualEnchantmentEvents.MarkedTarget>> MARKS = new HashMap<>();
   private static final Map<UUID, RitualEnchantmentEvents.PendingBreak> PENDING_BREAKS = new HashMap<>();
   private static final Map<UUID, Integer> VEIL_TICKS = new HashMap<>();
   private static final Map<UUID, Long> VEIL_ACTIVE = new HashMap<>();
   private static final Map<UUID, RitualEnchantmentEvents.ShearCheck> SHEAR_CHECKS = new HashMap<>();
   private static final Map<UUID, Long> SHEEP_REGROW = new HashMap<>();
   private static final Map<UUID, RitualEnchantmentEvents.HitWindow> SEAM_WINDOWS = new HashMap<>();
   private static final Map<UUID, Integer> SEAM_HITS = new HashMap<>();
   private static final Map<UUID, Long> KNOT_UNTIL = new HashMap<>();
   private static final Map<UUID, RitualEnchantmentEvents.HeadingTurn> HEADING_TURNS = new HashMap<>();

   private RitualEnchantmentEvents() {
   }

   private static int level(ItemStack stack, String id, Level level) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("mahoudim", id));
         Holder<Enchantment> holder = (Holder<Enchantment>)level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(key).orElse(null);
         return holder == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
      }
   }

   private static int armorLevel(LivingEntity entity, String id, Level level) {
      int result = 0;

      for (ItemStack stack : entity.getArmorSlots()) {
         result = Math.max(result, level(stack, id, level));
      }

      return result;
   }

   private static long now(Entity entity) {
      return entity.level().getGameTime();
   }

   private static void sendTo(ServerPlayer player, ParticleOptions particle, Vec3 pos, int count) {
      if (player.level() instanceof ServerLevel level) {
         level.sendParticles(player, particle, false, pos.x, pos.y, pos.z, count, 0.25, 0.35, 0.25, 0.01);
      }
   }

   private static boolean hasLineOfSight(ServerPlayer player, Entity target) {
      return player.hasLineOfSight(target);
   }

   @SubscribeEvent
   public static void onProjectileJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity joined = event.getEntity();
         if (joined instanceof FishingHook hook && hook.getOwner() instanceof ServerPlayer owner) {
            CompoundTag data = owner.getPersistentData();
            long until = data.getLong("mahoudim_tide_until");
            int tide = data.getInt("mahoudim_tide_level");
            if (until >= now(owner) && tide > 0) {
               hook.setDeltaMovement(hook.getDeltaMovement().scale(tide == 1 ? 1.05 : 1.1));
               hook.hurtMarked = true;
               data.remove("mahoudim_tide_until");
               data.remove("mahoudim_tide_level");
            }
         } else if (joined instanceof AbstractArrow arrow && arrow.getOwner() instanceof ServerPlayer shooter) {
            int mark = Math.max(
               level(shooter.getMainHandItem(), "marca_de_retorno", shooter.level()), level(shooter.getOffhandItem(), "marca_de_retorno", shooter.level())
            );
            if (mark > 0) {
               arrow.getPersistentData().putInt("mahoudim_return_mark_level", mark);
            }

            int umbral = tridentEnchantmentLevel(shooter);
            if (arrow instanceof ThrownTrident && umbral > 0) {
               arrow.getPersistentData().putInt("mahoudim_umbral_trident_level", umbral);
            }
         }
      }
   }

   private static int tridentEnchantmentLevel(ServerPlayer player) {
      int result = Math.max(
         level(player.getMainHandItem(), "umbral_de_impacto", player.level()), level(player.getOffhandItem(), "umbral_de_impacto", player.level())
      );
      if (result > 0) {
         return result;
      } else {
         for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.TRIDENT)) {
               result = Math.max(result, level(stack, "umbral_de_impacto", player.level()));
            }
         }

         for (ItemStack stackx : player.getInventory().offhand) {
            if (stackx.is(Items.TRIDENT)) {
               result = Math.max(result, level(stackx, "umbral_de_impacto", player.level()));
            }
         }

         return result;
      }
   }

   private static boolean hasEnchantedTrident(ServerPlayer player) {
      return tridentEnchantmentLevel(player) > 0;
   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
         Projectile projectile = event.getProjectile();
         if (projectile.getOwner() instanceof ServerPlayer shooter && projectile.level() instanceof ServerLevel level) {
            int markLevel = projectile.getPersistentData().getInt("mahoudim_return_mark_level");
            if (markLevel > 0) {
               MARKS.computeIfAbsent(shooter.getUUID(), ignored -> new ArrayList<>())
                  .add(new RitualEnchantmentEvents.MarkedTarget(target.getUUID(), level.dimension(), now(target) + 60L, markLevel));
            }

            int umbralLevel = projectile.getPersistentData().getInt("mahoudim_umbral_trident_level");
            if (umbralLevel > 0) {
               applyUmbral(target, shooter, umbralLevel);
            }
         }
      }
   }

   private static void applyUmbral(LivingEntity target, LivingEntity attacker, int enchantmentLevel) {
      long tick = now(target);
      Long transitioned = WATER_TRANSITIONS.get(target.getUUID());
      long previous = UMBRAL_COOLDOWN.getOrDefault(target.getUUID(), 0L);
      if (enchantmentLevel <= 0 && attacker instanceof ServerPlayer player) {
         enchantmentLevel = tridentEnchantmentLevel(player);
      }

      if (transitioned != null && tick - transitioned <= 10L && tick >= previous && enchantmentLevel > 0) {
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, enchantmentLevel == 1 ? 10 : 12, enchantmentLevel - 1));
         UMBRAL_COOLDOWN.put(target.getUUID(), tick + 160L);
      }
   }

   @SubscribeEvent
   public static void onDamage(Post event) {
      if (!(event.getNewDamage() <= 0.0F)) {
         LivingEntity victim = event.getEntity();
         long tick = now(victim);
         Entity source = event.getSource().getEntity();
         Entity direct = event.getSource().getDirectEntity();
         if (source instanceof LivingEntity attacker && direct == attacker) {
            int cadence = Math.max(
               level(attacker.getMainHandItem(), "cadencia_suspendida", attacker.level()),
               level(attacker.getOffhandItem(), "cadencia_suspendida", attacker.level())
            );
            if (cadence > 0) {
               RitualEnchantmentEvents.HitChain old = HIT_CHAINS.get(attacker.getUUID());
               int hits = old != null && old.target.equals(victim.getUUID()) && tick - old.lastTick <= 40L ? old.hits + 1 : 1;
               if (hits >= 4) {
                  applyCadence(victim, cadence);
                  HIT_CHAINS.remove(attacker.getUUID());
               } else {
                  HIT_CHAINS.put(attacker.getUUID(), new RitualEnchantmentEvents.HitChain(victim.getUUID(), tick, hits));
               }
            }

            int umbral = Math.max(
               level(attacker.getMainHandItem(), "umbral_de_impacto", attacker.level()),
               level(attacker.getOffhandItem(), "umbral_de_impacto", attacker.level())
            );
            if (umbral > 0) {
               applyUmbral(victim, attacker, umbral);
            }

            int inertia = Math.max(
               level(attacker.getMainHandItem(), "ancla_de_inercia", attacker.level()), level(attacker.getOffhandItem(), "ancla_de_inercia", attacker.level())
            );
            if (inertia > 0) {
               LAST_TARGET.put(attacker.getUUID(), victim.getUUID());
               INERTIA_LEVELS.put(attacker.getUUID(), inertia);
            }

            int eco = Math.max(
               level(attacker.getMainHandItem(), "eco_de_compas", attacker.level()), level(attacker.getOffhandItem(), "eco_de_compas", attacker.level())
            );
            RitualEnchantmentEvents.BlockedHit blocked = BLOCKED_HITS.get(attacker.getUUID());
            if (eco > 0 && blocked != null && blocked.attacker.equals(victim.getUUID()) && tick <= blocked.until) {
               if (attacker instanceof ServerPlayer blocker) {
                  sendTo(blocker, ParticleTypes.HAPPY_VILLAGER, victim.position().add(0.0, 1.0, 0.0), 8);
               }

               BLOCKED_HITS.remove(attacker.getUUID());
            }
         }

         if (victim instanceof ServerPlayer player) {
            UUID aggressor = source == null ? null : source.getUUID();
            UUID last = LAST_TARGET.get(player.getUUID());
            if (last != null && last.equals(aggressor)) {
               INERTIA_READY.put(player.getUUID(), tick + 20L);
               INERTIA_READY_LEVEL.put(player.getUUID(), INERTIA_LEVELS.getOrDefault(player.getUUID(), 1));
            }

            int guard = level(player.getItemBySlot(EquipmentSlot.CHEST), "pliegue_de_guardia", player.level());
            if (guard > 0 && tick >= GUARD_COOLDOWN.getOrDefault(player.getUUID(), 0L)) {
               GUARD_READY.put(player.getUUID(), tick + 40L);
               GUARD_COOLDOWN.put(player.getUUID(), tick + 200L);
            }

            int seam = armorLevel(player, "costura_de_instante", player.level());
            if (seam > 0) {
               RitualEnchantmentEvents.HitWindow previous = SEAM_WINDOWS.get(player.getUUID());
               int hits = previous != null && tick - previous.tick <= (seam == 1 ? 40 : 60) ? SEAM_HITS.getOrDefault(player.getUUID(), 0) + 1 : 1;
               SEAM_WINDOWS.put(
                  player.getUUID(),
                  new RitualEnchantmentEvents.HitWindow(tick, source == null ? player.position().subtract(player.getLookAngle()) : source.position())
               );
               SEAM_HITS.put(player.getUUID(), hits);
               if (hits >= 2) {
                  Vec3 origin = source == null ? player.position().subtract(player.getLookAngle()) : source.position();
                  sendTo(player, ParticleTypes.CRIT, origin.add(0.0, 1.0, 0.0), seam == 1 ? 6 : 8);
                  SEAM_HITS.put(player.getUUID(), 0);
               }
            }
         }
      }
   }

   private static void applyCadence(LivingEntity target, int level) {
      AttributeInstance attribute = target.getAttribute(Attributes.ATTACK_SPEED);
      if (attribute != null) {
         attribute.removeModifier(CADENCE_MODIFIER);
         attribute.addTransientModifier(new AttributeModifier(CADENCE_MODIFIER, -0.15 * level, Operation.ADD_MULTIPLIED_TOTAL));
         CADENCE_UNTIL.put(target.getUUID(), now(target) + 30L);
      }
   }

   @SubscribeEvent
   public static void onShieldBlock(LivingShieldBlockEvent event) {
      if (event.getOriginalBlock() && event.getBlocked() && event.getEntity() instanceof ServerPlayer player) {
         Entity attacker = event.getDamageSource().getEntity();
         if (attacker != null) {
            long tick = now(player);
            int eco = level(player.getOffhandItem(), "eco_de_compas", player.level());
            if (eco > 0) {
               BLOCKED_HITS.put(player.getUUID(), new RitualEnchantmentEvents.BlockedHit(attacker.getUUID(), tick + 40L));
            }

            int knot = level(player.getOffhandItem(), "nudo_de_anclaje", player.level());
            if (knot > 0 && event.getDamageSource().getDirectEntity() instanceof Projectile) {
               KNOT_UNTIL.put(player.getUUID(), tick + 20L);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      LivingEntity entity = event.getEntity();
      long tick = now(entity);
      if (entity instanceof ServerPlayer player) {
         Long guard = GUARD_READY.get(player.getUUID());
         if (guard != null && tick <= guard) {
            int level = player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
               ? 1
               : Math.max(1, level(player.getItemBySlot(EquipmentSlot.CHEST), "pliegue_de_guardia", player.level()));
            event.setStrength(event.getStrength() * (level == 1 ? 0.85F : 0.75F));
            GUARD_READY.remove(player.getUUID());
         }

         Long inertia = INERTIA_READY.get(player.getUUID());
         if (inertia != null && tick <= inertia) {
            int inertiaLevel = INERTIA_READY_LEVEL.getOrDefault(player.getUUID(), 1);
            event.setStrength(event.getStrength() * (inertiaLevel == 1 ? 0.9F : 0.8F));
            INERTIA_READY.remove(player.getUUID());
            INERTIA_READY_LEVEL.remove(player.getUUID());
         }

         Long knot = KNOT_UNTIL.get(player.getUUID());
         if (knot != null && tick <= knot) {
            int level = Math.max(1, level(player.getOffhandItem(), "nudo_de_anclaje", player.level()));
            event.setStrength(event.getStrength() * (level == 1 ? 0.9F : 0.8F));
            KNOT_UNTIL.remove(player.getUUID());
         }
      }
   }

   @SubscribeEvent
   public static void onBreak(BreakEvent event) {
      if (event.getPlayer() instanceof ServerPlayer player) {
         ServerLevel var19 = player.serverLevel();
         ItemStack tool = player.getMainHandItem();
         BlockState state = event.getState();
         long tick = var19.getGameTime();
         if (!tool.isEmpty() && state.getBlock().getDescriptionId().contains("ore") && level(tool, "pulso_de_estrato", player.level()) > 0) {
            pulseNearbyOres(player, event.getPos(), 3 + level(tool, "pulso_de_estrato", player.level()));
         }

         if (!tool.isEmpty()
            && (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE))
            && level(tool, "sondeo_de_veta", player.level()) > 0
            && player.getRandom().nextFloat() < 0.08F) {
            pulseCave(player, event.getPos());
         }

         int grid = level(tool, "reticula_de_cosecha", player.level());
         if (grid > 0
            && !player.getPersistentData().getBoolean("mahoudim_harvest_grid")
            && state.getBlock() instanceof CropBlock crop
            && crop.getAge(state) >= crop.getMaxAge()) {
            player.getPersistentData().putBoolean("mahoudim_harvest_grid", true);

            try {
               int harvested = 0;

               for (int dx = -1; dx <= 1 && harvested < (grid == 1 ? 4 : 8); dx++) {
                  for (int dz = -1; dz <= 1 && harvested < (grid == 1 ? 4 : 8); dz++) {
                     if (dx != 0 || dz != 0) {
                        BlockPos neighbor = event.getPos().offset(dx, 0, dz);
                        BlockState nearby = var19.getBlockState(neighbor);
                        if (nearby.getBlock() instanceof CropBlock nearbyCrop
                           && nearbyCrop.getAge(nearby) >= nearbyCrop.getMaxAge()
                           && player.gameMode.destroyBlock(neighbor)) {
                           harvested++;
                        }
                     }
                  }
               }
            } finally {
               player.getPersistentData().remove("mahoudim_harvest_grid");
            }
         }

         int wear = level(tool, "reloj_de_desgaste", player.level());
         if (wear > 0 && !tool.isEmpty()) {
            PENDING_BREAKS.put(
               player.getUUID(),
               new RitualEnchantmentEvents.PendingBreak(
                  event.getPos().immutable(), var19.dimension(), tick, BuiltInRegistries.ITEM.getKey(tool.getItem()), wear
               )
            );
         }
      }
   }

   private static void pulseNearbyOres(ServerPlayer player, BlockPos center, int radius) {
      ServerLevel level = player.serverLevel();
      int max = level(player.getMainHandItem(), "pulso_de_estrato", player.level()) == 1 ? 4 : 6;
      int emitted = 0;

      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
         if (emitted < max && level.getBlockState(pos).getBlock().getDescriptionId().contains("ore")) {
            boolean exposed = false;

            for (Direction direction : Direction.values()) {
               if (level.getBlockState(pos.relative(direction)).isAir()) {
                  exposed = true;
                  break;
               }
            }

            if (exposed && player.distanceToSqr(Vec3.atCenterOf(pos)) <= (radius + 2) * (radius + 2) && canSeeBlock(player, pos)) {
               sendTo(player, ParticleTypes.END_ROD, Vec3.atCenterOf(pos), 3);
               emitted++;
            }
         }
      }
   }

   private static boolean canSeeBlock(ServerPlayer player, BlockPos pos) {
      Vec3 from = player.getEyePosition();
      Vec3 to = Vec3.atCenterOf(pos);
      BlockHitResult hit = player.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, player));
      return hit.getType() == Type.MISS || hit.getBlockPos().equals(pos);
   }

   private static void pulseCave(ServerPlayer player, BlockPos center) {
      ServerLevel level = player.serverLevel();
      BlockPos found = null;
      double best = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16, -8, -16), center.offset(16, 8, 16))) {
         if (level.getBlockState(pos).isAir()) {
            int solid = 0;

            for (Direction direction : Direction.values()) {
               if (level.getBlockState(pos.relative(direction)).isSolid()) {
                  solid++;
               }
            }

            if (solid >= 2) {
               double dist = center.distSqr(pos);
               if (!(dist > 256.0) && !(dist < 4.0) && dist < best) {
                  best = dist;
                  found = pos.immutable();
               }
            }
         }
      }

      if (found != null) {
         Vec3 from = player.position().add(0.0, 1.0, 0.0);
         Vec3 to = Vec3.atCenterOf(found);

         for (int i = 1; i <= 5; i++) {
            sendTo(player, ParticleTypes.END_ROD, from.lerp(to, i / 6.0), 1);
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && !(event.getDistance() < 1.5F) && !(event.getDistance() > 4.0F)) {
         int phase = level((ItemStack)player.getInventory().armor.get(0), "paso_de_fase", player.level());
         if (phase > 0) {
            player.getPersistentData().putLong("mahoudim_phase_step_until", now(player) + (phase == 1 ? 10L : 20L));
         }
      }
   }

   @SubscribeEvent
   public static void onChangeTarget(LivingChangeTargetEvent event) {
      if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && event.getEntity() instanceof Mob mob) {
         long var7 = VEIL_ACTIVE.getOrDefault(player.getUUID(), 0L);
         if (now(player) <= var7 && !(player.distanceToSqr(mob) <= 16.0)) {
            AttributeInstance follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (follow != null && mob.distanceToSqr(player) > Math.pow(follow.getValue() * 0.65, 2.0)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onShearAttempt(EntityInteract event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Sheep sheep) {
         int shear = level(event.getItemStack(), "corte_de_intervalo", player.level());
         if (shear > 0 && !sheep.isSheared()) {
            SHEAR_CHECKS.put(sheep.getUUID(), new RitualEnchantmentEvents.ShearCheck(player.getUUID(), now(player) + 2L, shear));
         }
      }
   }

   @SubscribeEvent
   public static void onFish(ItemFishedEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         int tide = Math.max(
            level(player.getMainHandItem(), "cuerda_de_marea", player.level()), level(player.getOffhandItem(), "cuerda_de_marea", player.level())
         );
         if (tide > 0 && !event.getDrops().isEmpty()) {
            player.getPersistentData().putLong("mahoudim_tide_until", now(player) + (tide == 1 ? 160L : 200L));
            player.getPersistentData().putInt("mahoudim_tide_level", tide);
         }
      }
   }

   @SubscribeEvent
   public static void onBell(RightClickBlock event) {
      if (event.getEntity() instanceof ServerPlayer player
         && event.getLevel() instanceof ServerLevel level
         && level.getBlockState(event.getPos()).getBlock() instanceof BellBlock) {
         int resonance = level(event.getItemStack(), "resonancia_de_campana", player.level());
         if (resonance > 0) {
            for (Villager villager : level.getEntitiesOfClass(Villager.class, new AABB(event.getPos()).inflate(16.0))) {
               sendTo(player, ParticleTypes.HAPPY_VILLAGER, villager.position().add(0.0, 1.0, 0.0), 4);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onCompassUse(RightClickItem event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getItemStack().is(Items.COMPASS)) {
         ItemStack compass = event.getItemStack();
         if (level(compass, "brujula_de_retornos", player.level()) > 0) {
            CompoundTag data = player.getPersistentData();
            boolean usePortal = !data.getBoolean("mahoudim_compass_portal_mode");
            GlobalPos target;
            if (usePortal && data.contains("mahoudim_last_portal_dim")) {
               ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(data.getString("mahoudim_last_portal_dim")));
               target = GlobalPos.of(dimension, BlockPos.of(data.getLong("mahoudim_last_portal_pos")));
            } else {
               ServerLevel overworld = player.serverLevel().getServer().overworld();
               target = GlobalPos.of(overworld.dimension(), overworld.getSharedSpawnPos());
               usePortal = false;
            }

            compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(target), false));
            data.putBoolean("mahoudim_compass_portal_mode", usePortal);
            player.displayClientMessage(Component.translatable(usePortal ? "mahoudim.msg.compass_portal" : "mahoudim.msg.compass_spawn"), true);
         }
      }
   }

   @SubscribeEvent
   public static void onClockUse(RightClickItem event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getItemStack().is(Items.CLOCK)) {
         int reserve = level(event.getItemStack(), "reserva_de_compas", player.level());
         if (reserve > 0 && player.level().isRaining()) {
            int rainTime = ((ServerLevelData)player.serverLevel().getLevelData()).getRainTime();
            int threshold = reserve == 1 ? 1200 : 2400;
            if (rainTime <= threshold) {
               player.displayClientMessage(Component.translatable("mahoudim.msg.rain_soon"), true);
               sendTo(player, ParticleTypes.END_ROD, player.position().add(0.0, 2.0, 0.0), 6);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CompoundTag data = player.getPersistentData();
         data.putString("mahoudim_last_portal_dim", player.level().dimension().location().toString());
         data.putLong("mahoudim_last_portal_pos", player.blockPosition().asLong());
         int level = level((ItemStack)player.getInventory().armor.get(1), "memoria_de_umbral", player.level());
         if (level > 0) {
            float cardinal = Math.round(player.getYRot() / 90.0F) * 90.0F;
            HEADING_TURNS.put(player.getUUID(), new RitualEnchantmentEvents.HeadingTurn(cardinal, now(player), 10));
         }
      }
   }

   @SubscribeEvent
   public static void onTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
      Entity entity = event.getEntity();
      if (!entity.level().isClientSide()) {
         long tick = now(entity);
         if (entity instanceof LivingEntity living) {
            boolean wet = living.isInWater();
            Boolean wasWet = WATER_STATE.put(living.getUUID(), wet);
            if (wasWet != null && wasWet != wet) {
               WATER_TRANSITIONS.put(living.getUUID(), tick);
            }

            Long slowUntil = CADENCE_UNTIL.get(living.getUUID());
            if (slowUntil != null && tick >= slowUntil) {
               AttributeInstance attribute = living.getAttribute(Attributes.ATTACK_SPEED);
               if (attribute != null) {
                  attribute.removeModifier(CADENCE_MODIFIER);
               }

               CADENCE_UNTIL.remove(living.getUUID());
            }

            if (living instanceof Sheep sheep) {
               tickSheep(sheep, tick);
            }
         }

         if (entity instanceof ServerPlayer player) {
            tickPlayer(player, tick);
         }

         if (entity instanceof FishingHook hook && hook.getOwner() instanceof ServerPlayer owner) {
            CompoundTag data = owner.getPersistentData();
            long until = data.getLong("mahoudim_tide_until");
            int tide = data.getInt("mahoudim_tide_level");
            if (tide > 0 && tick <= until) {
               double boost = tide == 1 ? 1.05 : 1.1;
               hook.setDeltaMovement(hook.getDeltaMovement().scale(boost));
               hook.hurtMarked = true;
               data.remove("mahoudim_tide_until");
               data.remove("mahoudim_tide_level");
            }
         }
      }
   }

   private static void tickSheep(Sheep sheep, long tick) {
      RitualEnchantmentEvents.ShearCheck check = SHEAR_CHECKS.get(sheep.getUUID());
      if (check != null && tick >= check.checkAt) {
         SHEAR_CHECKS.remove(sheep.getUUID());
         if (sheep.isSheared()) {
            SHEEP_REGROW.put(sheep.getUUID(), tick + Math.max(1200L, 6000L - check.level * 1200L));
         }
      }

      Long regrow = SHEEP_REGROW.get(sheep.getUUID());
      if (regrow != null && tick >= regrow && sheep.isAlive() && sheep.isSheared()) {
         sheep.setSheared(false);
         SHEEP_REGROW.remove(sheep.getUUID());
      }
   }

   private static void tickPlayer(ServerPlayer player, long tick) {
      RitualEnchantmentEvents.HeadingTurn turn = HEADING_TURNS.get(player.getUUID());
      if (turn != null) {
         long elapsed = tick - turn.startTick;
         if (elapsed >= turn.duration) {
            player.setYRot(turn.targetYaw);
            player.setYHeadRot(turn.targetYaw);
            HEADING_TURNS.remove(player.getUUID());
         } else {
            float difference = Mth.wrapDegrees(turn.targetYaw - player.getYRot());
            float yaw = player.getYRot() + difference / (float)(turn.duration - elapsed);
            player.setYRot(yaw);
            player.setYHeadRot(yaw);
         }
      }

      ItemStack helmet = (ItemStack)player.getInventory().armor.get(3);
      int veil = level(helmet, "velo_de_reloj", player.level());
      boolean still = player.isCrouching() && player.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4 && veil > 0;
      boolean nearbyCombat = !player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(4.0), LivingEntity::isAlive).isEmpty();
      int progress = still && !nearbyCombat ? VEIL_TICKS.getOrDefault(player.getUUID(), 0) + 1 : 0;
      VEIL_TICKS.put(player.getUUID(), progress);
      if (progress >= 60) {
         VEIL_ACTIVE.put(player.getUUID(), tick + 2L);
      }

      long phaseUntil = player.getPersistentData().getLong("mahoudim_phase_step_until");
      if (phaseUntil >= tick && player.level().getBlockState(player.blockPosition()).is(Blocks.POWDER_SNOW)) {
         Vec3 movement = player.getDeltaMovement();
         player.setDeltaMovement(movement.x * 1.45, movement.y, movement.z * 1.45);
         player.hurtMarked = true;
         player.getPersistentData().remove("mahoudim_phase_step_until");
      } else if (phaseUntil < tick) {
         player.getPersistentData().remove("mahoudim_phase_step_until");
      }

      List<RitualEnchantmentEvents.MarkedTarget> marked = MARKS.get(player.getUUID());
      if (marked != null) {
         Iterator<RitualEnchantmentEvents.MarkedTarget> iterator = marked.iterator();

         while (iterator.hasNext()) {
            RitualEnchantmentEvents.MarkedTarget mark = iterator.next();
            if (tick > mark.until) {
               iterator.remove();
            } else if (mark.dimension.equals(player.level().dimension())
               && player.serverLevel().getEntity(mark.target) instanceof LivingEntity target
               && target.isAlive()
               && !(player.distanceToSqr(target) > 1024.0)) {
               Vec3 look = player.getViewVector(1.0F).normalize();
               Vec3 toward = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(player.getEyePosition()).normalize();
               if (look.dot(toward) < 0.65 && hasLineOfSight(player, target) && tick % 8L == 0L) {
                  sendTo(player, ParticleTypes.END_ROD, target.position().add(0.0, target.getBbHeight() * 0.5, 0.0), mark.level == 1 ? 2 : 4);
               }
            }
         }

         if (marked.isEmpty()) {
            MARKS.remove(player.getUUID());
         }
      }

      RitualEnchantmentEvents.PendingBreak pending = PENDING_BREAKS.get(player.getUUID());
      if (pending != null && tick > pending.tick && player.level().dimension().equals(pending.dimension)) {
         if (player.level().getBlockState(pending.pos).isAir()) {
            ItemStack tool = player.getMainHandItem();
            int count = player.getPersistentData().getInt("mahoudim_wear_count") + 1;
            player.getPersistentData().putInt("mahoudim_wear_count", count);
            long last = player.getPersistentData().getLong("mahoudim_wear_last");
            int threshold = pending.level == 1 ? 16 : 12;
            if (count >= threshold && tick - last >= 240L && BuiltInRegistries.ITEM.getKey(tool.getItem()).equals(pending.item)) {
               player.getPersistentData().putInt("mahoudim_wear_count", 0);
               player.getPersistentData().putLong("mahoudim_wear_last", tick);
               if (tool.getDamageValue() > 0) {
                  tool.setDamageValue(tool.getDamageValue() - 1);
               }
            }
         }

         PENDING_BREAKS.remove(player.getUUID());
      }

      for (Entry<UUID, RitualEnchantmentEvents.BlockedHit> entry : new ArrayList<>(BLOCKED_HITS.entrySet())) {
         if (tick > entry.getValue().until) {
            BLOCKED_HITS.remove(entry.getKey());
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         long var5 = now(player);
         List<RitualEnchantmentEvents.MarkedTarget> marks = MARKS.get(player.getUUID());
         if (marks != null) {
            marks.removeIf(mark -> var5 > mark.until);
         }
      }
   }

   private record BlockedHit(UUID attacker, long until) {
   }

   private record HeadingTurn(float targetYaw, long startTick, int duration) {
   }

   private record HitChain(UUID target, long lastTick, int hits) {
   }

   private record HitWindow(long tick, Vec3 source) {
   }

   private record MarkedTarget(UUID target, ResourceKey<Level> dimension, long until, int level) {
   }

   private record PendingBreak(BlockPos pos, ResourceKey<Level> dimension, long tick, ResourceLocation item, int level) {
   }

   private record ShearCheck(UUID player, long checkAt, int level) {
   }
}

package jhojan.mahoudim.realm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jhojan.mahoudim.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags.Blocks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public final class RealmEvents {
   private static final TagKey<Block> COMPATIBLE_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("mahoudim", "compatible_ores"));

   private RealmEvents() {
   }

   @SubscribeEvent
   public static void onPlayerTick(Post e) {
      if (e.getEntity() instanceof ServerPlayer p) {
         if (p.tickCount % 20 == 0) {
            ServerLevel level = p.serverLevel();
            if (RealmManager.inRealm(level)) {
               RealmConfig cfg = RealmData.active(p);
               if (cfg.flow > 0) {
                  int amp = cfg.flow - 1;
                  p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, amp, true, false));
                  p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, amp, true, false));
               }

               int danger = cfg.effectiveDanger();
               if (danger >= 2 && p.tickCount % 100 == 0) {
                  for (Monster m : level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(32.0))) {
                     m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, danger - 2, true, false));
                     if (danger >= 3) {
                        m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, 0, true, false));
                        m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, true, false));
                     }
                  }
               }

               int pulse = Config.spawnPulseTicks();
               if (danger >= 2 && pulse > 0 && p.tickCount % pulse == 0) {
                  spawnPulse(p, level, cfg, danger);
               }
            }
         }
      }
   }

   private static void spawnPulse(ServerPlayer p, ServerLevel level, RealmConfig cfg, int danger) {
      int near = level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(40.0)).size();
      if (near <= 24) {
         List<EntityType<? extends Mob>> pool = switch (cfg.theme) {
            case MINING, MONSTER_FARM -> List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER, EntityType.ENDERMAN);
            case CHRONO, ARCHIVE -> List.of(EntityType.SKELETON, EntityType.ZOMBIE);
            case RIFT, LABYRINTH -> List.of(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.SHULKER);
            case FAE -> List.of(EntityType.WITCH, EntityType.SPIDER);
            case EMPTY -> List.of();
         };

         for (int i = 0; i < danger * 2; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0;
            double dist = 10.0 + level.random.nextDouble() * 8.0;
            int x = (int)Math.floor(p.getX() + Math.cos(ang) * dist);
            int z = (int)Math.floor(p.getZ() + Math.sin(ang) * dist);
            BlockPos spot = null;

            for (int y = (int)p.getY() + 4; y > (int)p.getY() - 8; y--) {
               BlockPos pos = new BlockPos(x, y, z);
               if (level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                  spot = pos;
                  break;
               }
            }

            if (spot != null) {
               EntityType<? extends Mob> type = pool.get(level.random.nextInt(pool.size()));
               type.spawn(level, spot, MobSpawnType.EVENT);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onBreak(BreakEvent e) {
      if (e.getPlayer() instanceof ServerPlayer p) {
         if (e.getLevel() instanceof ServerLevel level) {
            if (RealmManager.inRealm(level)) {
               BlockState state = e.getState();
               if (state.is(Blocks.ORES) || state.is(COMPATIBLE_ORES)) {
                  RealmConfig cfg = RealmData.active(p);
                  if (cfg.richness > 0) {
                     if (!(level.random.nextFloat() >= 0.25F * (float)cfg.richness)) {
                        BlockPos pos = e.getPos();

                        for (ItemStack s : Block.getDrops(state, level, pos, level.getBlockEntity(pos), p, p.getMainHandItem())) {
                           Block.popResource(level, pos, s.copy());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClone(Clone e) {
      CompoundTag old = e.getOriginal().getPersistentData();
      if (old.contains("mahoudim")) {
         e.getEntity().getPersistentData().put("mahoudim", old.getCompound("mahoudim").copy());
      }
   }

   @SubscribeEvent
   public static void onCommands(RegisterCommandsEvent e) {
      e.getDispatcher().register((LiteralArgumentBuilder)Commands.literal("mahoudim").then(Commands.literal("return").executes(ctx -> {
         CommandSourceStack src = (CommandSourceStack)ctx.getSource();
         if (src.getEntity() instanceof ServerPlayer p) {
            RealmManager.returnHome(p);
         }

         return 1;
      })));
   }
}

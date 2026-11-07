package jhojan.mahoudim.tech;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public final class TechniqueEvents {
   private static final String FROZEN_TAG = "mahoudim_frozen";
   private static final int SAMPLE_EVERY = 10;
   private static final int MAX_SAMPLES = 40;
   private static final Map<UUID, ArrayDeque<TechniqueEvents.Snapshot>> HISTORY = new HashMap<>();
   private static final Map<UUID, TechniqueEvents.Frozen> FROZEN = new HashMap<>();
   private static final Map<UUID, TechniqueEvents.Accel> ACCEL = new HashMap<>();

   private TechniqueEvents() {
   }

   @SubscribeEvent
   public static void onPlayerTick(Post e) {
      if (e.getEntity() instanceof ServerPlayer p) {
         if (p.tickCount % 10 == 0 && p.isAlive()) {
            ArrayDeque<TechniqueEvents.Snapshot> q = HISTORY.computeIfAbsent(p.getUUID(), k -> new ArrayDeque<>());
            q.addLast(
               new TechniqueEvents.Snapshot(
                  p.level().dimension(), p.getX(), p.getY(), p.getZ(), p.getYRot(), p.getXRot(), p.getHealth(), p.getFoodData().getFoodLevel()
               )
            );

            while (q.size() > 40) {
               q.removeFirst();
            }
         }
      }
   }

   public static TechniqueEvents.Snapshot snapshotBack(ServerPlayer p, int seconds) {
      ArrayDeque<TechniqueEvents.Snapshot> q = HISTORY.get(p.getUUID());
      if (q != null && q.size() >= 2) {
         TechniqueEvents.Snapshot[] arr = q.toArray(new TechniqueEvents.Snapshot[0]);
         int idx = Math.max(0, arr.length - 1 - seconds * 2);
         return arr[idx];
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent e) {
      HISTORY.remove(e.getEntity().getUUID());
      ACCEL.remove(e.getEntity().getUUID());
   }

   public static void freeze(Entity e, long until) {
      if (e instanceof Mob m) {
         m.setNoAi(true);
      } else {
         e.setNoGravity(true);
      }

      e.getPersistentData().putBoolean("mahoudim_frozen", true);
      FROZEN.put(e.getUUID(), new TechniqueEvents.Frozen(e.level().dimension(), until));
   }

   private static void unfreeze(Entity e) {
      if (e instanceof Mob m) {
         m.setNoAi(false);
      } else {
         e.setNoGravity(false);
      }

      e.getPersistentData().remove("mahoudim_frozen");
      FROZEN.remove(e.getUUID());
   }

   @SubscribeEvent
   public static void onJoin(EntityJoinLevelEvent e) {
      if (!e.getLevel().isClientSide()) {
         Entity ent = e.getEntity();
         if (ent.getPersistentData().getBoolean("mahoudim_frozen") && !FROZEN.containsKey(ent.getUUID())) {
            unfreeze(ent);
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent e) {
      for (UUID id : new ArrayList<>(FROZEN.keySet())) {
         for (ServerLevel l : e.getServer().getAllLevels()) {
            Entity ent = l.getEntity(id);
            if (ent != null) {
               unfreeze(ent);
               break;
            }
         }
      }

      FROZEN.clear();
      ACCEL.clear();
      HISTORY.clear();
   }

   public static void startAcceleration(ServerPlayer p, long until, int radius) {
      ACCEL.put(p.getUUID(), new TechniqueEvents.Accel(until, radius));
   }

   @SubscribeEvent
   public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post e) {
      if (e.getLevel() instanceof ServerLevel level) {
         long var15 = level.getGameTime();
         if (!FROZEN.isEmpty()) {
            Iterator<Entry<UUID, TechniqueEvents.Frozen>> it = FROZEN.entrySet().iterator();

            while (it.hasNext()) {
               Entry<UUID, TechniqueEvents.Frozen> en = it.next();
               TechniqueEvents.Frozen f = en.getValue();
               if (f.dim().equals(level.dimension())) {
                  Entity ent = level.getEntity(en.getKey());
                  if (ent == null || !ent.isAlive()) {
                     it.remove();
                  } else if (var15 >= f.until()) {
                     it.remove();
                     if (ent instanceof Mob m) {
                        m.setNoAi(false);
                     } else {
                        ent.setNoGravity(false);
                     }

                     ent.getPersistentData().remove("mahoudim_frozen");
                  } else {
                     ent.setDeltaMovement(Vec3.ZERO);
                     ent.hurtMarked = true;
                  }
               }
            }
         }

         if (!ACCEL.isEmpty()) {
            Iterator<Entry<UUID, TechniqueEvents.Accel>> it = ACCEL.entrySet().iterator();

            while (it.hasNext()) {
               Entry<UUID, TechniqueEvents.Accel> en = it.next();
               TechniqueEvents.Accel a = en.getValue();
               ServerPlayer p = level.getServer().getPlayerList().getPlayer(en.getKey());
               if (p == null || var15 >= a.until()) {
                  it.remove();
               } else if (p.level() == level) {
                  BlockPos c = p.blockPosition();

                  for (int i = 0; i < 40; i++) {
                     int dx = level.random.nextInt(a.radius() * 2 + 1) - a.radius();
                     int dy = level.random.nextInt(a.radius() * 2 + 1) - a.radius();
                     int dz = level.random.nextInt(a.radius() * 2 + 1) - a.radius();
                     BlockPos pos = c.offset(dx, dy, dz);
                     BlockState st = level.getBlockState(pos);
                     if (st.isRandomlyTicking()) {
                        st.randomTick(level, pos, level.random);
                     }
                  }
               }
            }
         }
      }
   }

   private static record Accel(long until, int radius) {
   }

   private static record Frozen(ResourceKey<Level> dim, long until) {
   }

   public static record Snapshot(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch, float health, int food) {
   }
}

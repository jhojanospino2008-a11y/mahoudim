package jhojan.mahoudim.realm;

import jhojan.mahoudim.Config;
import jhojan.mahoudim.RecipeHook;
import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.GateCircleBlock;
import jhojan.mahoudim.tile.UtilityCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import stepsword.mahoutsukai.mana.PlayerManaManager;

public final class RealmManager {
   private RealmManager() {
   }

   public static boolean inRealm(Level level) {
      return level.dimension().location().getNamespace().equals("mahoudim");
   }

   public static boolean enter(ServerPlayer p, RealmConfig cfg) {
      cfg.sanitize();
      ServerLevel current = p.serverLevel();
      boolean fromRealm = inRealm(current);
      if (fromRealm && !Config.allowEnterFromRealm()) {
         p.sendSystemMessage(Component.translatable("mahoudim.msg.no_switch"));
         return false;
      } else {
         ResourceLocation dimId = cfg.dimensionId();
         ServerLevel target = p.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));
         if (target == null) {
            p.sendSystemMessage(Component.translatable("mahoudim.msg.no_dimension", new Object[]{dimId.toString()}));
            return false;
         } else {
            int cost = cfg.manaCost();
            if (PlayerManaManager.drainMana(p, cost, false, false) != cost) {
               p.sendSystemMessage(Component.translatable("mahoudim.msg.no_mana", new Object[]{cost}));
               return false;
            } else {
               CompoundTag root = RealmData.root(p);
               if (!fromRealm) {
                  CompoundTag ret = new CompoundTag();
                  ret.putString("dim", current.dimension().location().toString());
                  ret.putDouble("x", p.getX());
                  ret.putDouble("y", p.getY());
                  ret.putDouble("z", p.getZ());
                  ret.putFloat("yaw", p.getYRot());
                  ret.putFloat("pitch", p.getXRot());
                  root.put("return", ret);
               }

               int x = cfg.instance * 3000;
               int z = cfg.theme.ordinal() * 3000;
               target.getChunk(x >> 4, z >> 4);
               int top = target.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
               int minY = target.getMinBuildHeight();
               boolean enclosed = false;
               int y;
               if (cfg.theme == Theme.MINING) {
                  switch (cfg.depth) {
                     case 0:
                        y = top <= minY + 2 ? 70 : top + 1;
                        break;
                     case 1:
                        y = 16;
                        enclosed = true;
                        break;
                     default:
                        y = -48;
                        enclosed = true;
                  }
               } else if (cfg.theme == Theme.EMPTY) {
                  y = 80;
               } else {
                  y = top <= minY + 2 ? 100 : top + 1;
               }

               BlockPos center = new BlockPos(x, y, z);
               ensureChamber(target, center, enclosed, p);
               RealmData.setConfig(p, cfg);
               RealmData.setActive(p, cfg);
               p.teleportTo(target, (double)center.getX() + 0.5, (double)center.getY(), (double)center.getZ() + 2.5, p.getYRot(), p.getXRot());
               p.fallDistance = 0.0F;
               p.sendSystemMessage(Component.translatable("mahoudim.msg.entered", new Object[]{Component.translatable(cfg.theme.langKey())}));
               return true;
            }
         }
      }
   }

   public static boolean returnHome(ServerPlayer p) {
      if (!inRealm(p.level())) {
         p.sendSystemMessage(Component.translatable("mahoudim.msg.not_in_realm"));
         return false;
      } else {
         CompoundTag root = RealmData.root(p);
         ServerLevel dest = p.server.overworld();
         float yaw = p.getYRot();
         float pitch = p.getXRot();
         double x;
         double y;
         double z;
         if (root.contains("return")) {
            CompoundTag ret = root.getCompound("return");
            ResourceLocation id = ResourceLocation.tryParse(ret.getString("dim"));
            ServerLevel l = id == null ? null : p.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
            if (l != null) {
               dest = l;
            }

            x = ret.getDouble("x");
            y = ret.getDouble("y");
            z = ret.getDouble("z");
            yaw = ret.getFloat("yaw");
            pitch = ret.getFloat("pitch");
         } else {
            BlockPos sp = dest.getSharedSpawnPos();
            x = (double)sp.getX() + 0.5;
            y = (double)sp.getY();
            z = (double)sp.getZ() + 0.5;
         }

         dest.getChunk((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4);
         p.teleportTo(dest, x, y, z, yaw, pitch);
         p.fallDistance = 0.0F;
         root.remove("active");
         p.sendSystemMessage(Component.translatable("mahoudim.msg.returned"));
         return true;
      }
   }

   private static void ensureChamber(ServerLevel lv, BlockPos c, boolean enclosed, ServerPlayer owner) {
      if (!(lv.getBlockState(c).getBlock() instanceof GateCircleBlock)) {
         int r = 4;
         BlockState air = Blocks.AIR.defaultBlockState();
         BlockState floor = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
         BlockState lamp = Blocks.SEA_LANTERN.defaultBlockState();
         BlockState wall = Blocks.DEEPSLATE_BRICKS.defaultBlockState();

         for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
               boolean edge = Math.abs(dx) == 4 || Math.abs(dz) == 4;
               boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
               lv.setBlock(c.offset(dx, -1, dz), corner ? lamp : floor, 2);

               for (int dy = 0; dy <= 4; dy++) {
                  BlockPos pos = c.offset(dx, dy, dz);
                  boolean ceiling = dy == 4;
                  if (!enclosed || !edge && !ceiling) {
                     lv.setBlock(pos, air, 2);
                  } else {
                     lv.setBlock(pos, wall, 2);
                  }
               }
            }
         }

         lv.setBlock(c, ((GateCircleBlock)Reg.GATE.get()).defaultBlockState(), 3);
         if (lv.getBlockEntity(c) instanceof UtilityCircleTile tile) {
            tile.setCatalysts(RecipeHook.gateRecipe());
            tile.setCasterUUID(owner.getUUID());
            tile.activateVisual();
         }
      }
   }
}

package jhojan.mahoudim.realm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class RealmData {
   private static final String ROOT = "mahoudim";

   private RealmData() {
   }

   public static CompoundTag root(ServerPlayer p) {
      CompoundTag pd = p.getPersistentData();
      if (!pd.contains("mahoudim")) {
         pd.put("mahoudim", new CompoundTag());
      }

      return pd.getCompound("mahoudim");
   }

   public static RealmConfig config(ServerPlayer p) {
      CompoundTag r = root(p);
      return r.contains("cfg") ? RealmConfig.fromTag(r.getCompound("cfg")) : new RealmConfig().sanitize();
   }

   public static void setConfig(ServerPlayer p, RealmConfig c) {
      root(p).put("cfg", c.toTag());
   }

   public static RealmConfig active(ServerPlayer p) {
      CompoundTag r = root(p);
      return r.contains("active") ? RealmConfig.fromTag(r.getCompound("active")) : config(p);
   }

   public static void setActive(ServerPlayer p, RealmConfig c) {
      root(p).put("active", c.toTag());
   }
}

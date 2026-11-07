package jhojan.mahoudim.realm;

import jhojan.mahoudim.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class RealmConfig {
   public Theme theme = Theme.MINING;
   public Ore ore = Ore.ALL;
   public int depth = 1;
   public int danger = 1;
   public int richness = 1;
   public int flow = 0;
   public int instance = 0;

   public RealmConfig sanitize() {
      if (this.theme == null) {
         this.theme = Theme.MINING;
      }

      if (this.ore == null || this.theme != Theme.MINING && this.theme != Theme.EMPTY) {
         this.ore = Ore.ALL;
      }

      this.depth = this.theme != Theme.MINING && this.theme != Theme.EMPTY ? 0 : clamp(this.depth, 0, 2);
      this.danger = clamp(this.danger, 0, 3);
      this.richness = clamp(this.richness, 0, 3);
      this.flow = clamp(this.flow, 0, 2);
      this.instance = clamp(this.instance, 0, 99);
      return this;
   }

   public ResourceLocation dimensionId() {
      String path = this.theme == Theme.MINING ? this.theme.path + "_" + (this.ore == Ore.MODDED ? Ore.ALL.id : this.ore.id) : this.theme.path;
      return ResourceLocation.fromNamespaceAndPath("mahoudim", path);
   }

   public int effectiveDanger() {
      return this.danger == 0 ? 0 : Math.min(3, this.danger + this.ore.dangerBonus);
   }

   public int manaCost() {
      double c = (double)this.theme.baseCost
         * this.ore.costMult
         * (1.0 + 0.25 * (double)this.depth)
         * (1.0 + 0.15 * (double)(this.danger + this.richness + this.flow));
      return Config.scaled((int)Math.round(c));
   }

   public CompoundTag toTag() {
      CompoundTag t = new CompoundTag();
      t.putInt("theme", this.theme.ordinal());
      t.putInt("ore", this.ore.ordinal());
      t.putInt("depth", this.depth);
      t.putInt("danger", this.danger);
      t.putInt("richness", this.richness);
      t.putInt("flow", this.flow);
      t.putInt("instance", this.instance);
      return t;
   }

   public static RealmConfig fromTag(CompoundTag t) {
      RealmConfig c = new RealmConfig();
      if (t != null) {
         c.theme = Theme.byOrdinal(t.getInt("theme"));
         c.ore = Ore.byOrdinal(t.getInt("ore"));
         c.depth = t.contains("depth") ? t.getInt("depth") : 1;
         c.danger = t.contains("danger") ? t.getInt("danger") : 1;
         c.richness = t.contains("richness") ? t.getInt("richness") : 1;
         c.flow = t.getInt("flow");
         c.instance = t.getInt("instance");
      }

      return c.sanitize();
   }

   public RealmConfig copy() {
      return fromTag(this.toTag());
   }

   private static int clamp(int v, int lo, int hi) {
      return Math.max(lo, Math.min(hi, v));
   }
}

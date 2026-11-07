package jhojan.mahoudim.realm;

public enum Theme {
   MINING("vetas", 300),
   MONSTER_FARM("monster_farm", 450),
   EMPTY("empty", 250),
   CHRONO("chrono_isles", 600),
   RIFT("spatial_rift", 800),
   FAE("fae_glade", 500),
   ARCHIVE("temporal_archive", 700),
   LABYRINTH("spatial_labyrinth", 850);

   public final String path;
   public final int baseCost;

   private Theme(String path, int baseCost) {
      this.path = path;
      this.baseCost = baseCost;
   }

   public String langKey() {
      return "mahoudim.theme." + this.name().toLowerCase();
   }

   public static Theme byOrdinal(int i) {
      Theme[] v = values();
      return v[Math.floorMod(i, v.length)];
   }
}

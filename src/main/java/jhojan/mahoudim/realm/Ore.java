package jhojan.mahoudim.realm;

public enum Ore {
   ALL("all", 1.0, 0),
   MODDED("modded", 1.2, 1),
   COAL("coal", 0.6, 0),
   COPPER("copper", 0.7, 0),
   IRON("iron", 0.8, 0),
   GOLD("gold", 1.1, 1),
   REDSTONE("redstone", 1.0, 1),
   LAPIS("lapis", 1.0, 1),
   DIAMOND("diamond", 1.8, 2),
   EMERALD("emerald", 1.6, 2);

   public final String id;
   public final double costMult;
   public final int dangerBonus;

   private Ore(String id, double costMult, int dangerBonus) {
      this.id = id;
      this.costMult = costMult;
      this.dangerBonus = dangerBonus;
   }

   public String langKey() {
      return "mahoudim.ore." + this.id;
   }

   public static Ore byOrdinal(int i) {
      Ore[] v = values();
      return v[Math.floorMod(i, v.length)];
   }
}

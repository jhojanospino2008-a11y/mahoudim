package jhojan.mahoudim.tech;

import jhojan.mahoudim.Config;

public enum Technique {
   TEMPORAL_REWIND("temporal_rewind", true, 800, Technique.Cat.EMERALD, Technique.Cat.EYE, Technique.Cat.QUARTZ, 10),
   TIME_STASIS("time_stasis", true, 0, Technique.Cat.EMERALD, Technique.Cat.IRON, Technique.Cat.QUARTZ, 100, 8),
   CHRONO_ACCELERATION("chrono_acceleration", true, 600, Technique.Cat.EMERALD, Technique.Cat.GOLD, Technique.Cat.QUARTZ, 8, 30),
   TIME_DILATION("time_dilation", true, 700, Technique.Cat.DIAMOND, Technique.Cat.EMERALD, Technique.Cat.QUARTZ, 16, 30),
   CHRONO_SHIFT("chrono_shift", true, 500, Technique.Cat.EMERALD, Technique.Cat.ENDER, Technique.Cat.QUARTZ),
   TEMPORAL_BUBBLE("temporal_bubble", true, 950, Technique.Cat.EMERALD, Technique.Cat.DIAMOND, Technique.Cat.QUARTZ, 12, 15),
   TIME_LEAP("time_leap", true, 700, Technique.Cat.GOLD, Technique.Cat.ENDER, Technique.Cat.QUARTZ, 600),
   CHRONO_GARDEN("chrono_garden", true, 1100, Technique.Cat.EMERALD, Technique.Cat.GOLD, Technique.Cat.QUARTZ, 10, 20),
   TEMPORAL_REGENERATION("temporal_regeneration", true, 800, Technique.Cat.EMERALD, Technique.Cat.IRON, Technique.Cat.QUARTZ, 12),
   TIME_SHELTER("time_shelter", true, 1000, Technique.Cat.DIAMOND, Technique.Cat.IRON, Technique.Cat.QUARTZ, 10, 12),
   TEMPORAL_CORE("temporal_core", true, 1200, Technique.Cat.DIAMOND, Technique.Cat.EMERALD, Technique.Cat.QUARTZ),
   SPATIAL_STEP("spatial_step", false, 150, Technique.Cat.ENDER, Technique.Cat.GOLD, Technique.Cat.QUARTZ, 24),
   SPATIAL_ANCHOR("spatial_anchor", false, 250, Technique.Cat.ENDER, Technique.Cat.IRON, Technique.Cat.QUARTZ),
   SPATIAL_VACUUM("spatial_vacuum", false, 300, Technique.Cat.ENDER, Technique.Cat.EYE, Technique.Cat.GOLD, 20),
   SPATIAL_TUNNEL("spatial_tunnel", false, 900, Technique.Cat.DIAMOND, Technique.Cat.ENDER, Technique.Cat.GOLD, 24),
   SPATIAL_SWAP("spatial_swap", false, 500, Technique.Cat.ENDER, Technique.Cat.EMERALD, Technique.Cat.QUARTZ, 20),
   SPATIAL_BEACON("spatial_beacon", false, 650, Technique.Cat.ENDER, Technique.Cat.DIAMOND, Technique.Cat.EYE),
   SPATIAL_PULSE("spatial_pulse", false, 750, Technique.Cat.ENDER, Technique.Cat.GOLD, Technique.Cat.EYE, 12),
   SPATIAL_PHASE("spatial_phase", false, 900, Technique.Cat.ENDER, Technique.Cat.DIAMOND, Technique.Cat.QUARTZ, 10),
   VOID_STEP("void_step", false, 450, Technique.Cat.ENDER, Technique.Cat.IRON, Technique.Cat.EYE, 36),
   DIMENSION_TUNER("dimension_tuner", false, 700, Technique.Cat.ENDER, Technique.Cat.EMERALD, Technique.Cat.DIAMOND),
   MAHORA_ADAPTATION("mahora_adaptation", false, 1600, Technique.Cat.MAHORA, Technique.Cat.NETHERITE, Technique.Cat.SCULK),
   UNLIMITED_PROJECTION("unlimited_projection", false, 250, Technique.Cat.STONE, Technique.Cat.IRON, Technique.Cat.AMETHYST);

   public final String id;
   public final boolean time;
   public final int baseCost;
   public final Technique.Cat a;
   public final Technique.Cat b;
   public final Technique.Cat c;
   public final int[] params;

   private Technique(String id, boolean time, int baseCost, Technique.Cat a, Technique.Cat b, Technique.Cat c, int... params) {
      this.id = id;
      this.time = time;
      this.baseCost = baseCost;
      this.a = a;
      this.b = b;
      this.c = c;
      this.params = params;
   }

   public int cost() {
      return Config.scaled(this.baseCost);
   }

   public int param(int i, int def) {
      return i < this.params.length ? this.params[i] : def;
   }

   public String nameKey() {
      return "mahoudim.book." + this.id + ".name";
   }

   public String descKey() {
      return "mahoudim.book." + this.id + ".desc";
   }

   public static enum Cat {
      IRON,
      QUARTZ,
      EMERALD,
      DIAMOND,
      GOLD,
      ENDER,
      EYE,
      STONE,
      END_STONE,
      OBSIDIAN,
      AMETHYST,
      PRISMARINE,
      SCULK,
      COPPER,
      NETHERITE,
      MAHORA;
   }
}

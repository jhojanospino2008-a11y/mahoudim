package jhojan.mahoudim;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class Config {
   private static final Builder B = new Builder();
   public static final DoubleValue COST_MULTIPLIER = B.comment("Multiplicador global del costo de mana de todo lo del addon (puertas, tecnicas).")
      .defineInRange("costMultiplier", 1.0, 0.0, 100.0);
   public static final IntValue SPAWN_PULSE_SECONDS = B.comment("Cada cuantos segundos aparecen mobs extra en reinos con peligro >= 2 (0 = desactivado).")
      .defineInRange("spawnPulseSeconds", 30, 0, 3600);
   public static final BooleanValue ALLOW_ENTER_FROM_REALM = B.comment("Permite cambiar de reino directamente desde dentro de otro reino.")
      .define("allowEnterFromRealm", true);
   public static final ModConfigSpec SPEC = B.build();

   private Config() {
   }

   public static int scaled(int base) {
      double m = 1.0;

      try {
         m = (Double)COST_MULTIPLIER.get();
      } catch (Exception var4) {
      }

      return Math.max(0, (int)Math.round((double)base * m));
   }

   public static int spawnPulseTicks() {
      try {
         return (Integer)SPAWN_PULSE_SECONDS.get() * 20;
      } catch (Exception var1) {
         return 600;
      }
   }

   public static boolean allowEnterFromRealm() {
      try {
         return (Boolean)ALLOW_ENTER_FROM_REALM.get();
      } catch (Exception var1) {
         return true;
      }
   }
}

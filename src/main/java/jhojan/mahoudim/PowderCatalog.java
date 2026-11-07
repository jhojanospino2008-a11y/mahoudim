package jhojan.mahoudim;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public final class PowderCatalog {
   private static final PowderCatalog.Entry[] POWDERS = new PowderCatalog.Entry[]{
      new PowderCatalog.Entry("STONE", "powdered_stone", new Color(145, 145, 150)),
      new PowderCatalog.Entry("END_STONE", "powdered_end_stone", new Color(225, 215, 155)),
      new PowderCatalog.Entry("OBSIDIAN", "powdered_obsidian", new Color(105, 75, 145)),
      new PowderCatalog.Entry("AMETHYST", "powdered_amethyst", new Color(185, 95, 240)),
      new PowderCatalog.Entry("PRISMARINE", "powdered_prismarine", new Color(65, 210, 185)),
      new PowderCatalog.Entry("SCULK", "powdered_sculk", new Color(25, 130, 145)),
      new PowderCatalog.Entry("COPPER", "powdered_copper", new Color(220, 125, 55)),
      new PowderCatalog.Entry("NETHERITE", "powdered_netherite", new Color(95, 80, 65)),
      new PowderCatalog.Entry("MAHORA", "powdered_mahora", new Color(62, 82, 176))
   };

   private PowderCatalog() {
   }

   public static void register() {
      try {
         Field var0 = Class.forName("jhojan.mahoudim.Reg").getField("POWDERS");
         Map var1 = (Map)var0.get(null);
         Class var2 = Class.forName("stepsword.mahoutsukai.item.ModItems");
         Field var3 = var2.getField("catalystMap");
         Map var4 = (Map)var3.get(null);

         for (PowderCatalog.Entry var5 : POWDERS) {
            Object var9 = findEnumKey(var1, var5.category);
            Object var10 = var1.get(var9);
            Method var11 = var10.getClass().getMethod("get");
            Object var12 = var11.invoke(var10);
            Field var13 = var12.getClass().getField("COLOR");
            var13.set(var12, var5.color);
            String var14 = (String)var12.getClass().getMethod("getCatalystName").invoke(var12);
            var4.put(var14, var12);
         }
      } catch (ReflectiveOperationException var15) {
         throw new IllegalStateException("Could not register MahouDim custom catalyst powders with Mahou Tsukai", var15);
      }
   }

   private static Object findEnumKey(Map<?, ?> var0, String var1) {
      for (Object var2 : var0.keySet()) {
         if (var2 instanceof Enum && ((Enum)var2).name().equals(var1)) {
            return var2;
         }
      }

      throw new IllegalStateException("Missing MahouDim powder category: " + var1);
   }

   private static final class Entry {
      final String category;
      final String item;
      final Color color;

      Entry(String var1, String var2, Color var3) {
         this.category = var1;
         this.item = var2;
         this.color = var3;
      }
   }
}

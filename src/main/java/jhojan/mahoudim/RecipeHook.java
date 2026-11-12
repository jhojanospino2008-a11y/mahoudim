package jhojan.mahoudim;

import java.util.HashMap;
import java.util.Map;
import jhojan.mahoudim.enchantment.RitualEnchantment;
import jhojan.mahoudim.tech.Technique;
import net.minecraft.world.level.block.Block;
import stepsword.mahoutsukai.block.BlockBase;
import stepsword.mahoutsukai.block.spells.MahoujinRecipeRegistrar;
import stepsword.mahoutsukai.item.ItemBase;
import stepsword.mahoutsukai.util.UnorderedList;

public final class RecipeHook {
   private static final Map<String, UnorderedList> ALLOCATED = new HashMap<>();

   private RecipeHook() {
   }

   public static String name(Technique.Cat c) {
      return switch (c) {
         case IRON -> MahoujinRecipeRegistrar.POWDERED_IRON;
         case QUARTZ -> MahoujinRecipeRegistrar.POWDERED_QUARTZ;
         case EMERALD -> MahoujinRecipeRegistrar.POWDERED_EMERALD;
         case DIAMOND -> MahoujinRecipeRegistrar.POWDERED_DIAMOND;
         case GOLD -> MahoujinRecipeRegistrar.POWDERED_GOLD;
         case ENDER -> MahoujinRecipeRegistrar.POWDERED_ENDER;
         case EYE -> MahoujinRecipeRegistrar.POWDERED_EYE;
         case STONE -> "mahoudim:powdered_stone";
         case END_STONE -> "mahoudim:powdered_end_stone";
         case OBSIDIAN -> "mahoudim:powdered_obsidian";
         case AMETHYST -> "mahoudim:powdered_amethyst";
         case PRISMARINE -> "mahoudim:powdered_prismarine";
         case SCULK -> "mahoudim:powdered_sculk";
         case COPPER -> "mahoudim:powdered_copper";
         case NETHERITE -> "mahoudim:powdered_netherite";
         case MAHORA -> "mahoudim:powdered_mahora";
      };
   }

   public static UnorderedList recipe(Technique.Cat a, Technique.Cat b, Technique.Cat c) {
      return MahoujinRecipeRegistrar.makeRecipe(name(a), name(b), name(c));
   }

   public static UnorderedList recipe(Technique t) {
      return ALLOCATED.getOrDefault(t.id, recipe(t.a, t.b, t.c));
   }

   public static UnorderedList gateRecipe() {
      return ALLOCATED.getOrDefault("gate", recipe(Technique.Cat.DIAMOND, Technique.Cat.EMERALD, Technique.Cat.ENDER));
   }

   public static UnorderedList pocketRecipe() {
      return ALLOCATED.getOrDefault("pocket", recipe(Technique.Cat.EMERALD, Technique.Cat.ENDER, Technique.Cat.IRON));
   }

   public static UnorderedList juadanaRitualRecipe() {
      return ALLOCATED.getOrDefault("juadana_ritual", recipe(Technique.Cat.NETHERITE, Technique.Cat.MAHORA, Technique.Cat.SCULK));
   }

   public static UnorderedList enchantmentRecipe(RitualEnchantment enchantment) {
      return ALLOCATED.getOrDefault("enchantment_" + enchantment.id, recipe(enchantment.a, enchantment.b, enchantment.c));
   }

   public static boolean isCarpetRitual(UnorderedList recipe) {
      if (recipe == null) {
         return false;
      }
      return ALLOCATED.entrySet().stream().anyMatch(entry ->
         (entry.getKey().equals("juadana_ritual") || entry.getKey().equals("unlimited_projection") || entry.getKey().startsWith("enchantment_"))
            && entry.getValue().equals(recipe)
      );
   }

   public static void register() {
      ALLOCATED.clear();

      for (Technique.Cat cat : new Technique.Cat[]{
         Technique.Cat.STONE,
         Technique.Cat.END_STONE,
         Technique.Cat.OBSIDIAN,
         Technique.Cat.AMETHYST,
         Technique.Cat.PRISMARINE,
         Technique.Cat.SCULK,
         Technique.Cat.COPPER,
         Technique.Cat.NETHERITE,
         Technique.Cat.MAHORA
      }) {
         MahoujinRecipeRegistrar.ingredients.put(name(cat), (ItemBase)Reg.POWDERS.get(cat).get());
      }

      put("gate", recipe(Technique.Cat.DIAMOND, Technique.Cat.EMERALD, Technique.Cat.ENDER), (Block)Reg.GATE.get(), false);
      put("pocket", recipe(Technique.Cat.EMERALD, Technique.Cat.ENDER, Technique.Cat.IRON), (Block)Reg.POCKET.get(), false);
      put("juadana_ritual", recipe(Technique.Cat.NETHERITE, Technique.Cat.MAHORA, Technique.Cat.SCULK), (Block)Reg.JUADANA_RITUAL_CIRCLE.get(), false);

      for (RitualEnchantment enchantment : RitualEnchantment.values()) {
         put(
            "enchantment_" + enchantment.id,
            recipe(enchantment.a, enchantment.b, enchantment.c),
            (Block)Reg.ENCHANTMENT_CIRCLES.get(enchantment).get(),
            false
         );
      }

      for (Technique t : Technique.values()) {
         boolean cloth = switch (t) {
            case TEMPORAL_BUBBLE, TIME_LEAP, CHRONO_GARDEN, TEMPORAL_REGENERATION, TIME_SHELTER, SPATIAL_SWAP, SPATIAL_BEACON, SPATIAL_PULSE, SPATIAL_PHASE, VOID_STEP, TEMPORAL_CORE, DIMENSION_TUNER -> false;
            default -> true;
         };
         put(t.id, recipe(t.a, t.b, t.c), (Block)Reg.TECH_BLOCKS.get(t).get(), cloth);
      }

      MahouDim.LOGGER.info("[mahoudim] {} recetas únicas registradas tras Mahou Tsukai", ALLOCATED.size());
   }

   private static void put(String key, UnorderedList preferred, Block block, boolean cloth) {
      UnorderedList list = freeRecipe(preferred);
      ALLOCATED.put(key, list);
      MahoujinRecipeRegistrar.recipes.put(list, (BlockBase)block);
      MahoujinRecipeRegistrar.needsCloth.put(list, cloth);
   }

   private static UnorderedList freeRecipe(UnorderedList preferred) {
      if (!MahoujinRecipeRegistrar.recipes.containsKey(preferred)) {
         return preferred;
      } else {
         Technique.Cat[] cats = Technique.Cat.values();

         for (Technique.Cat a : cats) {
            for (Technique.Cat b : cats) {
               for (Technique.Cat c : cats) {
                  UnorderedList candidate = recipe(a, b, c);
                  if (!MahoujinRecipeRegistrar.recipes.containsKey(candidate) && !ALLOCATED.containsValue(candidate)) {
                     return candidate;
                  }
               }
            }
         }

         throw new IllegalStateException("No quedan combinaciones de polvos libres para MahouDim");
      }
   }
}

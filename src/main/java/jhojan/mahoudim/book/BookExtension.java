package jhojan.mahoudim.book;

import java.util.ArrayList;
import jhojan.mahoudim.Config;
import jhojan.mahoudim.RecipeHook;
import jhojan.mahoudim.tech.Technique;
import stepsword.mahoutsukai.item.guide.ContentsPage;
import stepsword.mahoutsukai.item.guide.InfoPage;
import stepsword.mahoutsukai.item.guide.Page;
import stepsword.mahoutsukai.item.guide.SpellPage;

public final class BookExtension {
   private static final int PER_CONTENTS_PAGE = 28;

   private BookExtension() {
   }

   private static InfoPage info(String id) {
      return new InfoPage("mahoudim.book." + id + ".name", "mahoudim.book." + id + ".desc", "");
   }

   public static int extend(ArrayList<Page> pages) {
      while (!pages.isEmpty() && pages.get(0) instanceof ContentsPage) {
         pages.remove(0);
      }

      pages.add(info("intro"));
      pages.add(info("realms"));
      pages.add(info("gate_use"));
      pages.add(info("mining"));
      pages.add(info("ores"));
      pages.add(info("chrono_isles"));
      pages.add(info("spatial_rift"));
      pages.add(info("fae_glade"));
      pages.add(info("modifiers"));
      pages.add(info("return"));
      pages.add(
         new SpellPage("mahoudim.book.dimensional_gate.name", RecipeHook.gateRecipe(), "mahoudim.book.dimensional_gate.desc")
            .setValues(new Integer[]{Config.scaled(300)})
      );
      pages.add(
         new SpellPage("mahoudim.book.spatial_pocket.name", RecipeHook.pocketRecipe(), "mahoudim.book.spatial_pocket.desc")
            .setValues(new Integer[]{Config.scaled(40)})
      );
      pages.add(info("time_arts"));

      for (Technique t : Technique.values()) {
         if (t.time) {
            pages.add(techniquePage(t));
         }
      }

      pages.add(info("space_arts"));

      for (Technique tx : Technique.values()) {
         if (!tx.time && tx != Technique.MAHORA_ADAPTATION) {
            pages.add(techniquePage(tx));
         }
      }

      pages.add(info("mahora_arts"));
      pages.add(techniquePage(Technique.MAHORA_ADAPTATION));

      int n = Math.max(1, (pages.size() + 28 - 1) / 28);
      ContentsPage[] contents = new ContentsPage[n];

      for (int i = 0; i < n; i++) {
         contents[i] = new ContentsPage(pages, i * 28, (i + 1) * 28);
      }

      for (int i = n - 1; i >= 0; i--) {
         pages.add(0, contents[i]);
      }

      return n;
   }

   private static SpellPage techniquePage(Technique t) {
      Integer[] vals = new Integer[1 + t.params.length];
      vals[0] = t.cost();

      for (int i = 0; i < t.params.length; i++) {
         vals[i + 1] = t.params[i];
      }

      return new SpellPage(t.nameKey(), RecipeHook.recipe(t), t.descKey()).setValues(vals);
   }
}

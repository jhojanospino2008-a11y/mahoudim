package jhojan.mahoudim.client;

import jhojan.mahoudim.item.SecondSorceryCode;
import jhojan.mahoudim.item.TechniqueScroll;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.UnlimitedProjectionSpell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;

public final class ProjectionPreviewEvents {
   private static final int[][] RING = new int[][]{
      {-1, -4},
      {0, -4},
      {1, -4},
      {-2, -3},
      {2, -3},
      {-3, -2},
      {3, -2},
      {-4, -1},
      {4, -1},
      {-4, 0},
      {4, 0},
      {-4, 1},
      {4, 1},
      {-3, 2},
      {3, 2},
      {-2, 3},
      {2, 3},
      {-1, 4},
      {0, 4},
      {1, 4}
   };

   private ProjectionPreviewEvents() {
   }

   @SubscribeEvent
   public static void renderTargetIndicator(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player != null && minecraft.screen == null) {
         ItemStack scroll = activeProjectionScroll(player);
         if (!scroll.isEmpty() && UnlimitedProjectionSpell.getRememberedStack(scroll).isEmpty()) {
            if (!UnlimitedProjectionSpell.findTargetStack(player).isEmpty()) {
               GuiGraphics graphics = event.getGuiGraphics();
               int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
               int centerY = minecraft.getWindow().getGuiScaledHeight() / 2 + 8;

               for (int[] point : RING) {
                  int x = centerX + point[0];
                  int y = centerY + point[1];
                  graphics.fill(x - 1, y - 1, x + 3, y + 3, -1442840576);
                  graphics.fill(x, y, x + 2, y + 2, -1);
               }
            }
         }
      }
   }

   private static ItemStack activeProjectionScroll(Player player) {
      ItemStack using = player.getUseItem();
      ItemStack fromUse = projectionScroll(using);
      if (!fromUse.isEmpty()) {
         return fromUse;
      } else {
         if (using.getItem() instanceof SecondSorceryCode) {
            ItemStack selected = selectedCodeScroll(using);
            if (!selected.isEmpty()) {
               return selected;
            }
         }

         ItemStack main = player.getMainHandItem();
         ItemStack directMain = projectionScroll(main);
         if (!directMain.isEmpty()) {
            return directMain;
         } else {
            ItemStack directOff = projectionScroll(player.getOffhandItem());
            if (!directOff.isEmpty()) {
               return directOff;
            } else {
               ItemStack fromMainCode = selectedCodeScroll(main);
               return !fromMainCode.isEmpty() ? fromMainCode : selectedCodeScroll(player.getOffhandItem());
            }
         }
      }
   }

   private static ItemStack projectionScroll(ItemStack stack) {
      return stack.getItem() instanceof TechniqueScroll scroll && scroll.technique() == Technique.UNLIMITED_PROJECTION ? stack : ItemStack.EMPTY;
   }

   private static ItemStack selectedCodeScroll(ItemStack code) {
      if (!(code.getItem() instanceof SecondSorceryCode)) {
         return ItemStack.EMPTY;
      } else {
         int slot = MysticCode.getSlot(code);
         return slot >= 0 && slot < 6 ? projectionScroll(SecondSorceryCode.inventory(code).getStackInSlot(slot)) : ItemStack.EMPTY;
      }
   }
}

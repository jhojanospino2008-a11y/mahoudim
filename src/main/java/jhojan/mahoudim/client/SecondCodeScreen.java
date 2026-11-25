package jhojan.mahoudim.client;

import jhojan.mahoudim.menu.SecondCodeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SecondCodeScreen extends AbstractContainerScreen<SecondCodeMenu> {
   public SecondCodeScreen(SecondCodeMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 176;
      this.imageHeight = 166;
      this.inventoryLabelY = 72;
   }

   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, -15263453);
      graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, -13618111);
      graphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + 17, -14671059);
      graphics.fill(x + 8, y + 18, x + this.imageWidth - 8, y + 65, -14078664);
      graphics.fill(x + 5, y + 76, x + this.imageWidth - 5, y + 160, -14078664);
   }

   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 6, 15128474, false);
      graphics.drawString(this.font, Component.translatable("container.mahoudim.second_sorcery_slots"), 8, 67, 14146018, false);
      graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 14146018, false);
   }
}

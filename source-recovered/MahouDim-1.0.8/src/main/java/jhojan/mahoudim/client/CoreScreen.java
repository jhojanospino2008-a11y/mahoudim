package jhojan.mahoudim.client;

import jhojan.mahoudim.net.CoreActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class CoreScreen extends Screen {
   private int radius = 10;
   private int seconds = 20;
   private int multiplier = 4;

   public CoreScreen() {
      super(Component.translatable("mahoudim.core.title"));
   }

   protected void init() {
      int x = (this.width - 260) / 2;
      int y = (this.height - 130) / 2;
      this.addRenderableWidget(Button.builder(Component.literal("Radio: 10"), b -> {
         this.radius = this.radius >= 16 ? 4 : this.radius + 2;
         b.setMessage(Component.literal("Radio: " + this.radius));
      }).bounds(x, y, 260, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Duración: 20 s"), b -> {
         this.seconds = this.seconds >= 60 ? 10 : this.seconds + 10;
         b.setMessage(Component.literal("Duración: " + this.seconds + " s"));
      }).bounds(x, y + 25, 260, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Velocidad: 4x"), b -> {
         this.multiplier = this.multiplier >= 16 ? 1 : this.multiplier * 2;
         b.setMessage(Component.literal("Velocidad: " + this.multiplier + "x"));
      }).bounds(x, y + 50, 260, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.core.activate"), b -> {
         PacketDistributor.sendToServer(new CoreActionPacket(this.radius, this.seconds, this.multiplier), new CustomPacketPayload[0]);
         this.onClose();
      }).bounds(x, y + 80, 126, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.gui.close"), b -> this.onClose()).bounds(x + 134, y + 80, 126, 20).build());
   }

   public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
      super.renderBackground(g, mx, my, pt);
      int x = (this.width - 280) / 2;
      int y = (this.height - 160) / 2;
      g.fill(x, y, x + 280, y + 160, -300807644);
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      super.render(g, mx, my, pt);
      g.drawCenteredString(this.font, this.title, this.width / 2, (this.height - 130) / 2 - 20, 15128831);
      g.drawCenteredString(this.font, Component.translatable("mahoudim.core.hint"), this.width / 2, (this.height - 130) / 2 + 125, 12303291);
   }

   public boolean isPauseScreen() {
      return false;
   }
}

package jhojan.mahoudim.client;

import jhojan.mahoudim.net.GateActionPacket;
import jhojan.mahoudim.realm.Ore;
import jhojan.mahoudim.realm.RealmConfig;
import jhojan.mahoudim.realm.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class GateScreen extends Screen {
   private static final int W = 300;
   private static final int H = 236;
   private final BlockPos pos;
   private final RealmConfig cfg;
   private final boolean inRealm;
   private Button themeBtn;
   private Button oreBtn;
   private Button depthBtn;
   private Button dangerBtn;
   private Button richBtn;
   private Button flowBtn;
   private Button zoneBtn;

   public GateScreen(BlockPos pos, RealmConfig cfg, boolean inRealm) {
      super(Component.translatable("mahoudim.gui.title"));
      this.pos = pos;
      this.cfg = cfg;
      this.inRealm = inRealm;
   }

   private int left() {
      return (this.width - 300) / 2;
   }

   private int top() {
      return (this.height - 236) / 2;
   }

   protected void init() {
      int x = this.left() + 20;
      int y = this.top() + 26;
      int bw = 260;
      this.themeBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.theme = Theme.byOrdinal(this.cfg.theme.ordinal() + 1);
         this.cfg.sanitize();
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.oreBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.ore = Ore.byOrdinal(this.cfg.ore.ordinal() + 1);
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.depthBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.depth = (this.cfg.depth + 1) % 3;
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.dangerBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.danger = (this.cfg.danger + 1) % 4;
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.richBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.richness = (this.cfg.richness + 1) % 4;
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.flowBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.flow = (this.cfg.flow + 1) % 3;
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      y += 22;
      this.zoneBtn = (Button)this.addRenderableWidget(Button.builder(Component.empty(), b -> {
         this.cfg.instance = (this.cfg.instance + 1) % 100;
         this.refresh();
      }).bounds(x, y, bw, 18).build());
      int by = this.top() + 236 - 28;
      int w3 = (bw - 8) / 3;
      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.gui.open"), b -> {
         this.send(1);
         this.onClose();
      }).bounds(x, by, w3, 20).build());
      Button ret = (Button)this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.gui.return"), b -> {
         this.send(2);
         this.onClose();
      }).bounds(x + w3 + 4, by, w3, 20).build());
      ret.active = this.inRealm;
      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.gui.close"), b -> this.onClose()).bounds(x + (w3 + 4) * 2, by, w3, 20).build());
      this.refresh();
   }

   private void refresh() {
      this.cfg.sanitize();
      boolean mining = this.cfg.theme == Theme.MINING || this.cfg.theme == Theme.EMPTY;
      this.themeBtn.setMessage(label("mahoudim.gui.theme", Component.translatable(this.cfg.theme.langKey())));
      this.oreBtn.setMessage(label("mahoudim.gui.ore", Component.translatable(this.cfg.ore.langKey())));
      this.oreBtn.active = mining;
      this.depthBtn.setMessage(label("mahoudim.gui.depth", Component.translatable("mahoudim.depth." + this.cfg.depth)));
      this.depthBtn.active = mining;
      this.dangerBtn.setMessage(label("mahoudim.gui.danger", Component.translatable("mahoudim.level." + this.cfg.danger)));
      this.richBtn.setMessage(label("mahoudim.gui.richness", Component.translatable("mahoudim.level." + this.cfg.richness)));
      this.flowBtn.setMessage(label("mahoudim.gui.flow", Component.translatable("mahoudim.flow." + this.cfg.flow)));
      this.zoneBtn.setMessage(label("mahoudim.gui.zone", Component.literal(String.valueOf(this.cfg.instance + 1))));
   }

   private static Component label(String key, Component value) {
      return Component.translatable(key).append(": ").append(value);
   }

   private void send(int action) {
      PacketDistributor.sendToServer(new GateActionPacket(this.pos, this.cfg.toTag(), action), new CustomPacketPayload[0]);
   }

   public void onClose() {
      this.send(0);
      super.onClose();
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
      super.renderBackground(g, mouseX, mouseY, partial);
      int l = this.left();
      int t = this.top();
      g.fill(l, t, l + 300, t + 236, -300807644);
      g.fill(l + 2, t + 2, l + 300 - 2, t + 236 - 2, -300019144);
      g.fill(l + 2, t + 20, l + 300 - 2, t + 21, -8758017);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
      super.render(g, mouseX, mouseY, partial);
      int l = this.left();
      int t = this.top();
      g.drawCenteredString(this.font, this.title, l + 150, t + 8, 15128831);
      g.drawCenteredString(this.font, Component.translatable("mahoudim.gui.cost", new Object[]{this.cfg.manaCost()}), l + 150, t + 236 - 62, 10478591);
      g.drawCenteredString(
         this.font,
         Component.translatable(
            "mahoudim.gui.hint." + (this.cfg.theme != Theme.MINING && this.cfg.theme != Theme.EMPTY ? this.cfg.theme.name().toLowerCase() : "mining")
         ),
         l + 150,
         t + 236 - 50,
         12303291
      );
      g.drawCenteredString(
         this.font, Component.translatable("mahoudim.gui.effective_danger", new Object[]{this.cfg.effectiveDanger()}), l + 150, t + 236 - 40, 12303291
      );
   }

   public boolean isPauseScreen() {
      return false;
   }
}

package jhojan.mahoudim.client;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import jhojan.mahoudim.net.EffectActionPacket;
import jhojan.mahoudim.net.OpenEffectScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EffectTechniqueScreen extends Screen {
   private final OpenEffectScreenPacket request;
   private final List<ResourceLocation> allEffectIds = new ArrayList<>();
   private final List<ResourceLocation> effectIds = new ArrayList<>();
   private int selected;
   private String searchText = "";
   private EditBox searchBox;
   private EditBox levelBox;
   private EditBox durationBox;
   private Button particlesButton;
   private boolean particles = true;

   public EffectTechniqueScreen(OpenEffectScreenPacket request) {
      super(Component.translatable(request.kind() == 0 ? "mahoudim.effect.title_increment" : "mahoudim.effect.title_create"));
      this.request = request;
      if (request.kind() == 0 && Minecraft.getInstance().player != null) {
         for (MobEffectInstance active : Minecraft.getInstance().player.getActiveEffects()) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)active.getEffect().value());
            if (id != null && !this.effectIds.contains(id)) {
               this.allEffectIds.add(id);
            }
         }
      } else {
         for (ResourceLocation id : BuiltInRegistries.MOB_EFFECT.keySet()) {
            this.allEffectIds.add(id);
         }
      }

      this.allEffectIds.sort(Comparator.comparing(ResourceLocation::toString));
      this.filterEffects();
   }

   protected void init() {
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.cycle(-1)).bounds(x + 15, y + 29, 28, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.cycle(1)).bounds(x + 257, y + 29, 28, 20).build());
      this.searchBox = new EditBox(this.font, x + 48, y + 29, 204, 20, Component.translatable("mahoudim.effect.search"));
      this.searchBox.setMaxLength(64);
      this.searchBox.setHint(Component.translatable("mahoudim.effect.search_hint"));
      this.searchBox.setValue(this.searchText);
      this.searchBox.setResponder(value -> {
         this.searchText = value;
         this.filterEffects();
      });
      this.addRenderableWidget(this.searchBox);
      this.levelBox = new EditBox(this.font, x + 110, y + 81, 80, 20, Component.translatable("mahoudim.effect.level"));
      this.levelBox.setMaxLength(3);
      this.levelBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
      this.levelBox.setValue(this.defaultLevel());
      this.addRenderableWidget(this.levelBox);
      if (this.request.kind() == 1) {
         this.durationBox = new EditBox(this.font, x + 110, y + 108, 80, 20, Component.translatable("mahoudim.effect.duration"));
         this.durationBox.setMaxLength(5);
         this.durationBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
         this.durationBox.setValue("30");
         this.addRenderableWidget(this.durationBox);
         this.particlesButton = (Button)this.addRenderableWidget(Button.builder(this.particlesMessage(), button -> {
            this.particles = !this.particles;
            button.setMessage(this.particlesMessage());
         }).bounds(x + 40, y + 140, 220, 20).build());
      }

      this.addRenderableWidget(
         Button.builder(Component.translatable("mahoudim.effect.apply"), button -> this.submit()).bounds(x + 35, y + 163, 110, 20).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.translatable("mahoudim.effect.cancel"), button -> this.onClose()).bounds(x + 155, y + 163, 110, 20).build()
      );
      this.setFocused(this.searchBox);
   }

   private void filterEffects() {
      String query = normalize(this.searchText);
      this.effectIds.clear();

      for (ResourceLocation id : this.allEffectIds) {
         if (query.isEmpty()
            || normalize(id.toString()).contains(query)
            || normalize(Component.translatable(((MobEffect)BuiltInRegistries.MOB_EFFECT.get(id)).getDescriptionId()).getString()).contains(query)) {
            this.effectIds.add(id);
         }
      }

      this.selected = 0;
      if (this.levelBox != null && this.request.kind() == 0) {
         this.levelBox.setValue(this.defaultLevel());
      }
   }

   private static String normalize(String value) {
      return Normalizer.normalize(value, Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
   }

   private String defaultLevel() {
      if (this.request.kind() == 0 && Minecraft.getInstance().player != null && !this.effectIds.isEmpty()) {
         ResourceLocation selectedId = this.effectIds.get(this.selected);

         for (MobEffectInstance effect : Minecraft.getInstance().player.getActiveEffects()) {
            if (selectedId.equals(BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)effect.getEffect().value()))) {
               return Integer.toString(Math.min(256, effect.getAmplifier() + 2));
            }
         }
      }

      return "1";
   }

   private void cycle(int amount) {
      if (!this.effectIds.isEmpty()) {
         this.selected = Math.floorMod(this.selected + amount, this.effectIds.size());
         if (this.request.kind() == 0) {
            this.levelBox.setValue(this.defaultLevel());
         }
      }
   }

   private Component particlesMessage() {
      return Component.translatable(
         "mahoudim.effect.particles", new Object[]{Component.translatable(this.particles ? "mahoudim.effect.enabled" : "mahoudim.effect.disabled")}
      );
   }

   private void submit() {
      if (!this.effectIds.isEmpty()) {
         int duration = 0;

         int level;
         try {
            level = Integer.parseInt(this.levelBox.getValue());
            if (this.durationBox != null) {
               duration = Integer.parseInt(this.durationBox.getValue());
            }
         } catch (NumberFormatException var4) {
            return;
         }

         if (level >= 1 && level <= 256 && (this.durationBox == null || duration >= 1 && duration <= 72000)) {
            ResourceLocation effectId = this.effectIds.get(this.selected);
            PacketDistributor.sendToServer(
               new EffectActionPacket(this.request.kind(), effectId, level, duration, this.particles, this.request.outputMode()), new CustomPacketPayload[0]
            );
            this.onClose();
         }
      }
   }

   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(graphics, mouseX, mouseY, partialTick);
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      graphics.fill(x, y, x + 300, y + 190, -266855901);
      graphics.fill(x + 2, y + 2, x + 298, y + 21, -14078664);
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      graphics.drawCenteredString(this.font, this.title, this.width / 2, y + 7, 15128474);
      Component selectedName = this.effectIds.isEmpty()
         ? Component.translatable("mahoudim.effect.none")
         : Component.translatable(((MobEffect)BuiltInRegistries.MOB_EFFECT.get(this.effectIds.get(this.selected))).getDescriptionId());
      graphics.drawCenteredString(this.font, selectedName, this.width / 2, y + 54, 14149631);
      if (!this.effectIds.isEmpty()) {
         graphics.drawCenteredString(this.font, Component.literal(this.effectIds.get(this.selected).toString()), this.width / 2, y + 67, 10265524);
         if (this.request.kind() == 0 && Minecraft.getInstance().player != null) {
            ResourceLocation id = this.effectIds.get(this.selected);

            for (MobEffectInstance active : Minecraft.getInstance().player.getActiveEffects()) {
               if (id.equals(BuiltInRegistries.MOB_EFFECT.getKey((MobEffect)active.getEffect().value()))) {
                  graphics.drawCenteredString(
                     this.font,
                     Component.translatable("mahoudim.effect.current_level", new Object[]{active.getAmplifier() + 1}),
                     this.width / 2,
                     y + 77,
                     12044248
                  );
                  break;
               }
            }
         }
      }

      graphics.drawString(this.font, Component.translatable("mahoudim.effect.level"), x + 32, y + 88, 14146018, false);
      if (this.durationBox != null) {
         graphics.drawString(this.font, Component.translatable("mahoudim.effect.duration"), x + 32, y + 115, 14146018, false);

         String outputKey = switch (this.request.outputMode()) {
            case 1 -> "mahoudim.effect.output_book";
            case 2 -> "mahoudim.effect.output_potion";
            default -> "mahoudim.effect.output_player";
         };
         graphics.drawCenteredString(this.font, Component.translatable(outputKey), this.width / 2, y + 133, 10265524);
      } else {
         graphics.drawCenteredString(this.font, Component.translatable("mahoudim.effect.increment_hint"), this.width / 2, y + 118, 10265524);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}

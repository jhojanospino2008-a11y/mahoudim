package jhojan.mahoudim.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.text.Normalizer;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.PacketDistributor;
import jhojan.mahoudim.net.EffectActionPacket;
import jhojan.mahoudim.net.OpenEffectScreenPacket;
import jhojan.mahoudim.tech.EffectTechniqueActions;

/** Client-side selector for Effect Increment and Effect Create. */
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
      super(Component.translatable(request.kind() == EffectTechniqueActions.KIND_INCREMENT
         ? "mahoudim.effect.title_increment" : "mahoudim.effect.title_create"));
      this.request = request;
      if (request.kind() == EffectTechniqueActions.KIND_INCREMENT && Minecraft.getInstance().player != null) {
         for (MobEffectInstance active : Minecraft.getInstance().player.getActiveEffects()) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(active.getEffect().value());
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

   @Override
   protected void init() {
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.cycle(-1))
         .bounds(x + 15, y + 29, 28, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.cycle(1))
         .bounds(x + 257, y + 29, 28, 20).build());

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

      if (this.request.kind() == EffectTechniqueActions.KIND_CREATE) {
         this.durationBox = new EditBox(this.font, x + 110, y + 108, 80, 20, Component.translatable("mahoudim.effect.duration"));
         this.durationBox.setMaxLength(5);
         this.durationBox.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
         this.durationBox.setValue("30");
         this.addRenderableWidget(this.durationBox);
         this.particlesButton = this.addRenderableWidget(Button.builder(this.particlesMessage(), button -> {
            this.particles = !this.particles;
            button.setMessage(this.particlesMessage());
         }).bounds(x + 40, y + 140, 220, 20).build());
      }

      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.effect.apply"), button -> this.submit())
         .bounds(x + 35, y + 163, 110, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("mahoudim.effect.cancel"), button -> this.onClose())
         .bounds(x + 155, y + 163, 110, 20).build());
      this.setFocused(this.searchBox);
   }

   private void filterEffects() {
      String query = normalize(this.searchText);
      this.effectIds.clear();
      for (ResourceLocation id : this.allEffectIds) {
         if (query.isEmpty() || normalize(id.toString()).contains(query)
            || normalize(Component.translatable(BuiltInRegistries.MOB_EFFECT.get(id).getDescriptionId()).getString()).contains(query)) {
            this.effectIds.add(id);
         }
      }
      this.selected = 0;
      if (this.levelBox != null && this.request.kind() == EffectTechniqueActions.KIND_INCREMENT) {
         this.levelBox.setValue(this.defaultLevel());
      }
   }

   private static String normalize(String value) {
      return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
   }

   private String defaultLevel() {
      if (this.request.kind() == EffectTechniqueActions.KIND_INCREMENT && Minecraft.getInstance().player != null
         && !this.effectIds.isEmpty()) {
         ResourceLocation selectedId = this.effectIds.get(this.selected);
         for (MobEffectInstance effect : Minecraft.getInstance().player.getActiveEffects()) {
            if (selectedId.equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()))) {
               return Integer.toString(Math.min(EffectTechniqueActions.MAX_LEVEL, effect.getAmplifier() + 2));
            }
         }
      }
      return "1";
   }

   private void cycle(int amount) {
      if (this.effectIds.isEmpty()) {
         return;
      }
      this.selected = Math.floorMod(this.selected + amount, this.effectIds.size());
      if (this.request.kind() == EffectTechniqueActions.KIND_INCREMENT) {
         this.levelBox.setValue(this.defaultLevel());
      }
   }

   private Component particlesMessage() {
      return Component.translatable("mahoudim.effect.particles", Component.translatable(
         this.particles ? "mahoudim.effect.enabled" : "mahoudim.effect.disabled"));
   }

   private void submit() {
      if (this.effectIds.isEmpty()) {
         return;
      }
      int level;
      int duration = 0;
      try {
         level = Integer.parseInt(this.levelBox.getValue());
         if (this.durationBox != null) {
            duration = Integer.parseInt(this.durationBox.getValue());
         }
      } catch (NumberFormatException ex) {
         return;
      }
      if (level < 1 || level > EffectTechniqueActions.MAX_LEVEL
         || (this.durationBox != null && (duration < 1 || duration > EffectTechniqueActions.MAX_DURATION_SECONDS))) {
         return;
      }
      ResourceLocation effectId = this.effectIds.get(this.selected);
      PacketDistributor.sendToServer(new EffectActionPacket(
         this.request.kind(), effectId, level, duration, this.particles, this.request.outputMode()
      ), new CustomPacketPayload[0]);
      this.onClose();
   }

   @Override
   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(graphics, mouseX, mouseY, partialTick);
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      graphics.fill(x, y, x + 300, y + 190, 0xF0181A23);
      graphics.fill(x + 2, y + 2, x + 298, y + 21, 0xFF292D38);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      int x = (this.width - 300) / 2;
      int y = (this.height - 190) / 2;
      graphics.drawCenteredString(this.font, this.title, this.width / 2, y + 7, 0xE6D79A);
      Component selectedName = this.effectIds.isEmpty()
         ? Component.translatable("mahoudim.effect.none")
         : Component.translatable(BuiltInRegistries.MOB_EFFECT.get(this.effectIds.get(this.selected)).getDescriptionId());
      graphics.drawCenteredString(this.font, selectedName, this.width / 2, y + 54, 0xD7E7FF);
      if (!this.effectIds.isEmpty()) {
         graphics.drawCenteredString(this.font, Component.literal(this.effectIds.get(this.selected).toString()), this.width / 2, y + 67, 0x9CA3B4);
         if (this.request.kind() == EffectTechniqueActions.KIND_INCREMENT && Minecraft.getInstance().player != null) {
            ResourceLocation id = this.effectIds.get(this.selected);
            for (MobEffectInstance active : Minecraft.getInstance().player.getActiveEffects()) {
               if (id.equals(BuiltInRegistries.MOB_EFFECT.getKey(active.getEffect().value()))) {
                  graphics.drawCenteredString(this.font,
                     Component.translatable("mahoudim.effect.current_level", active.getAmplifier() + 1),
                     this.width / 2, y + 77, 0xB7C7D8);
                  break;
               }
            }
         }
      }
      graphics.drawString(this.font, Component.translatable("mahoudim.effect.level"), x + 32, y + 88, 0xD7D9E2, false);
      if (this.durationBox != null) {
         graphics.drawString(this.font, Component.translatable("mahoudim.effect.duration"), x + 32, y + 115, 0xD7D9E2, false);
         String outputKey = switch (this.request.outputMode()) {
            case EffectTechniqueActions.MODE_BOOK -> "mahoudim.effect.output_book";
            case EffectTechniqueActions.MODE_POTION -> "mahoudim.effect.output_potion";
            default -> "mahoudim.effect.output_player";
         };
         graphics.drawCenteredString(this.font, Component.translatable(outputKey), this.width / 2, y + 133, 0x9CA3B4);
      } else {
         graphics.drawCenteredString(this.font, Component.translatable("mahoudim.effect.increment_hint"), this.width / 2, y + 118, 0x9CA3B4);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}

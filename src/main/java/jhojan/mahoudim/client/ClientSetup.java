package jhojan.mahoudim.client;

import jhojan.mahoudim.Reg;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import stepsword.mahoutsukai.render.item.MagicalItemRender;
import stepsword.mahoutsukai.render.tile.MahoujinRenderer;

public final class ClientSetup {
   private ClientSetup() {
   }

   public static void init(IEventBus bus) {
      bus.addListener(ClientSetup::clientSetup);
      bus.addListener(ClientSetup::registerMenuScreens);
      bus.addListener(ClientSetup::registerClientExtensions);
      bus.addListener(ClientSetup::registerModels);
   }

   private static void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(Reg.SECOND_CODE_MENU.get(), SecondCodeScreen::new);
   }

   private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      event.registerItem(new MagicalItemRender(SecondSorceryCodeRenderer::new), Reg.SECOND_SORCERY_CODE.get());
   }

   private static void registerModels(ModelEvent.RegisterAdditional event) {
      event.register(SecondSorceryCodeRenderer.MODEL);
   }

   private static void clientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         BlockEntityRenderers.register((BlockEntityType)Reg.UTILITY_TILE.get(), MahoujinRenderer::new);
         BlockEntityRenderers.register((BlockEntityType)Reg.TECHNIQUE_TILE.get(), MahoujinRenderer::new);
      });
   }
}

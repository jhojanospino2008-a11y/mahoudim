package jhojan.mahoudim.client;

import jhojan.mahoudim.Reg;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import stepsword.mahoutsukai.render.tile.MahoujinRenderer;

public final class ClientSetup {
   private ClientSetup() {
   }

   public static void init(IEventBus bus) {
      bus.addListener(ClientSetup::clientSetup);
   }

   private static void clientSetup(FMLClientSetupEvent e) {
      e.enqueueWork(() -> {
         BlockEntityRenderers.register((BlockEntityType)Reg.UTILITY_TILE.get(), MahoujinRenderer::new);
         BlockEntityRenderers.register((BlockEntityType)Reg.TECHNIQUE_TILE.get(), MahoujinRenderer::new);
      });
   }
}

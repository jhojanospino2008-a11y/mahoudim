package jhojan.mahoudim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jhojan.mahoudim.Reg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import stepsword.mahoutsukai.render.tile.MahoujinRenderer;

public final class ClientSetup {
   private static final Map<UUID, Float> WHEEL_ROTATIONS = new HashMap<>();

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

   private static void renderMahoraWheel(RenderPlayerEvent.Post event) {
      Player player = event.getEntity();
      ItemStack wheel = player.getItemBySlot(EquipmentSlot.HEAD);
      if (!wheel.is(Reg.MAHORA_WHEEL.get())) {
         return;
      }

      PlayerRenderer renderer = event.getRenderer();
      PoseStack poseStack = event.getPoseStack();
      int rotationStep = wheel.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
            .copyTag().getInt("mahora_rotation");
      float targetRotation = rotationStep * 45.0F;
      UUID playerId = player.getUUID();
      float currentRotation = WHEEL_ROTATIONS.getOrDefault(playerId, targetRotation);
      float rotationDelta = targetRotation - currentRotation;
      if (Math.abs(rotationDelta) > 0.1F) {
         currentRotation += rotationDelta * 0.25F;
         WHEEL_ROTATIONS.put(playerId, currentRotation);
      } else {
         WHEEL_ROTATIONS.put(playerId, targetRotation);
      }

      poseStack.pushPose();
      renderer.getModel().head.translateAndRotate(poseStack);
      // The Blockbench model is an item model centered around its own origin.
      poseStack.translate(0.0D, -0.35D, 0.0D);
      poseStack.scale(0.42F, 0.42F, 0.42F);
      poseStack.mulPose(Axis.YP.rotationDegrees(currentRotation));

      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      itemRenderer.renderStatic(
            player,
            wheel,
            // NONE avoids the model's HEAD display transform, which was moving it to the feet.
            ItemDisplayContext.NONE,
            false,
            poseStack,
            event.getMultiBufferSource(),
            player.level(),
            event.getPackedLight(),
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
            player.getId());
      poseStack.popPose();
   }
}

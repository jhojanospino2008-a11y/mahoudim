package jhojan.mahoudim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import stepsword.mahoutsukai.render.RenderBaseItem;
import stepsword.mahoutsukai.render.item.MysticCodeRenderer;

/** Reuses Mahou Tsukai's animated Mystic Code rings with the Second Sorcery Code model. */
public final class SecondSorceryCodeRenderer extends BlockEntityWithoutLevelRenderer {
   public static final ModelResourceLocation MODEL = RenderBaseItem.getMRL(
      ResourceLocation.fromNamespaceAndPath("mahoudim", "item/second_sorcery_code")
   );

   public SecondSorceryCodeRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
      super(dispatcher, modelSet);
   }

   @Override
   public void renderByItem(
      ItemStack stack,
      ItemDisplayContext displayContext,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay
   ) {
      MysticCodeRenderer.renderMysticCode(MODEL, stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
   }
}

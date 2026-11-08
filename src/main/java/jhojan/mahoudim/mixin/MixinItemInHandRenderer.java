package jhojan.mahoudim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Removes only the first-person attack/mining swing of the Second Sorcery Code. */
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
   @Invoker("renderArmWithItem")
   protected abstract void mahoudim$renderArmWithItem(
      AbstractClientPlayer player,
      float partialTicks,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equippedProgress,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int combinedLight
   );

   @Redirect(
      method = "renderHandsWithItems",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
         ordinal = 0
      )
   )
   private void mahoudim$renderMainHandWithoutCodeSwing(
      ItemInHandRenderer renderer,
      AbstractClientPlayer player,
      float partialTicks,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equippedProgress,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int combinedLight
   ) {
      mahoudim$renderWithoutCodeSwing(player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
   }

   @Redirect(
      method = "renderHandsWithItems",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
         ordinal = 1
      )
   )
   private void mahoudim$renderOffHandWithoutCodeSwing(
      ItemInHandRenderer renderer,
      AbstractClientPlayer player,
      float partialTicks,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equippedProgress,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int combinedLight
   ) {
      mahoudim$renderWithoutCodeSwing(player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
   }

   private void mahoudim$renderWithoutCodeSwing(
      AbstractClientPlayer player,
      float partialTicks,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equippedProgress,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int combinedLight
   ) {
      float adjustedSwing = stack.getItem() instanceof SecondSorceryCode ? 0.0F : swingProgress;
      mahoudim$renderArmWithItem(player, partialTicks, pitch, hand, adjustedSwing, stack, equippedProgress, poseStack, buffer, combinedLight);
   }
}

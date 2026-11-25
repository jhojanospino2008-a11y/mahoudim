package jhojan.mahoudim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import jhojan.mahoudim.client.ArcaneCircleRenderer;
import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;
import stepsword.mahoutsukai.render.item.MysticCodeRenderer;

@Mixin(
   value = {MysticCodeRenderer.class},
   remap = false
)
public abstract class MixinMysticCodeRenderer {
   private static final String RENDER_BASE_ITEM_CALL = "Lstepsword/mahoutsukai/render/RenderBaseItem;render(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/ModelResourceLocation;)V";

   @Redirect(
      method = {"renderMysticCode"},
      at = @At(
         value = "INVOKE",
         target = "Lstepsword/mahoutsukai/item/mysticcode/MysticCode;getColor(Lnet/minecraft/world/item/ItemStack;)I",
         remap = false
      ),
      remap = false
   )
   private static int mahoudim$alwaysShowSecondCodeRings(MysticCode code, ItemStack stack) {
      return stack.getItem() instanceof SecondSorceryCode ? 7 : code.getColor(stack);
   }

   @Inject(
      method = {"renderMysticCode"},
      at = {@At(
         value = "INVOKE",
         target = "Lstepsword/mahoutsukai/render/RenderBaseItem;render(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/ModelResourceLocation;)V",
         shift = Shift.AFTER,
         remap = false
      )},
      remap = false
   )
   private static void mahoudim$normalizeSecondCodeRingScale(
      ModelResourceLocation model,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay,
      CallbackInfo callback
   ) {
      if (stack.getItem() instanceof SecondSorceryCode && isHandContext(context)) {
         poseStack.pushPose();
         Matrix4f pose = poseStack.last().pose();
         Vector3f currentScale = pose.getScale(new Vector3f());
         Vector3f targetScale = targetScale(context);
         scaleColumn(pose, 0, targetScale.x / safeScale(currentScale.x));
         scaleColumn(pose, 1, targetScale.y / safeScale(currentScale.y));
         scaleColumn(pose, 2, targetScale.z / safeScale(currentScale.z));
         ArcaneCircleRenderer.render(poseStack, buffer);
      }
   }

   @Inject(
      method = {"renderMysticCode"},
      at = {@At("RETURN")},
      remap = false
   )
   private static void mahoudim$restoreSecondCodeRingScale(
      ModelResourceLocation model,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      int packedOverlay,
      CallbackInfo callback
   ) {
      if (stack.getItem() instanceof SecondSorceryCode && isHandContext(context)) {
         poseStack.popPose();
      }
   }

   private static boolean isHandContext(ItemDisplayContext context) {
      return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
         || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
         || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
         || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
   }

   private static Vector3f targetScale(ItemDisplayContext context) {
      return context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
         ? new Vector3f(1.08F, 1.05F, 1.05F)
         : new Vector3f(0.88F, 0.88F, 0.88F);
   }

   private static float safeScale(float value) {
      return Math.abs(value) < 1.0E-6F ? 1.0F : value;
   }

   private static void scaleColumn(Matrix4f matrix, int column, float factor) {
      switch (column) {
         case 0:
            matrix.m00(matrix.m00() * factor);
            matrix.m10(matrix.m10() * factor);
            matrix.m20(matrix.m20() * factor);
            break;
         case 1:
            matrix.m01(matrix.m01() * factor);
            matrix.m11(matrix.m11() * factor);
            matrix.m21(matrix.m21() * factor);
            break;
         case 2:
            matrix.m02(matrix.m02() * factor);
            matrix.m12(matrix.m12() * factor);
            matrix.m22(matrix.m22() * factor);
            break;
         default:
            throw new IllegalArgumentException("Invalid matrix column: " + column);
      }
   }
}

package jhojan.mahoudim.mixin;

import net.minecraft.client.renderer.block.model.BlockElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Allows the supplied Blockbench item models to retain their authored coordinates. */
@Mixin(BlockElement.Deserializer.class)
public abstract class MixinBlockElementBounds {
   private static final float MAHOUDIM_MIN_MODEL_EXTENT = -512.0F;
   private static final float MAHOUDIM_MAX_MODEL_EXTENT = 512.0F;

   @ModifyConstant(method = "getTo", constant = @Constant(floatValue = -16.0F))
   private float mahoudim$expandToMinimum(float original) {
      return MAHOUDIM_MIN_MODEL_EXTENT;
   }

   @ModifyConstant(method = "getTo", constant = @Constant(floatValue = 32.0F))
   private float mahoudim$expandToMaximum(float original) {
      return MAHOUDIM_MAX_MODEL_EXTENT;
   }

   @ModifyConstant(method = "getFrom", constant = @Constant(floatValue = -16.0F))
   private float mahoudim$expandFromMinimum(float original) {
      return MAHOUDIM_MIN_MODEL_EXTENT;
   }

   @ModifyConstant(method = "getFrom", constant = @Constant(floatValue = 32.0F))
   private float mahoudim$expandFromMaximum(float original) {
      return MAHOUDIM_MAX_MODEL_EXTENT;
   }
}

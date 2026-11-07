package jhojan.mahoudim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"stepsword.mahoutsukai.tile.MahoujinTileEntity"},
   remap = false
)
public abstract class MixinMahoujinTileEntity {
   @Inject(
      method = {"hasCloth"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void mahoudim$hideClothForAddonCircles(CallbackInfoReturnable<Boolean> var1) {
      String var2 = this.getClass().getName();
      if (var2.equals("jhojan.mahoudim.tile.UtilityCircleTile") || var2.equals("jhojan.mahoudim.tile.__technique_cloth_visible__")) {
         var1.setReturnValue(false);
      }
   }
}

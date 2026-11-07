package jhojan.mahoudim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"stepsword.mahoutsukai.render.tile.MahoujinRenderer"},
   remap = false
)
public abstract class MixinMahoujinRenderer {
   @Inject(
      method = {"mahoujinOn"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void mahoudim$useOriginalActiveAnimation(@Coerce Object var0, CallbackInfoReturnable<Boolean> var1) {
      String var2 = var0.getClass().getName();
      if (var2.equals("jhojan.mahoudim.tile.UtilityCircleTile")) {
         try {
            var1.setReturnValue((Boolean)var0.getClass().getMethod("isEnabled").invoke(var0));
         } catch (ReflectiveOperationException var3) {
            var1.setReturnValue(false);
         }
      } else if (var2.equals("jhojan.mahoudim.tile.TechniqueCircleTile")) {
         var1.setReturnValue(true);
      }
   }
}

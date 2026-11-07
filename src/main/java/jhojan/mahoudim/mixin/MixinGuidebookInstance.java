package jhojan.mahoudim.mixin;

import java.util.ArrayList;
import jhojan.mahoudim.book.BookExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stepsword.mahoutsukai.item.guide.GuidebookInstance;
import stepsword.mahoutsukai.item.guide.Page;

@Mixin({GuidebookInstance.class})
public abstract class MixinGuidebookInstance {
   @Shadow(
      remap = false
   )
   ArrayList<Page> pages;
   @Shadow(
      remap = false
   )
   int contentsCount;

   @Inject(
      method = {"createPages"},
      at = {@At("TAIL")},
      remap = false
   )
   private void mahoudim$extendBook(CallbackInfo ci) {
      this.contentsCount = BookExtension.extend(this.pages);
   }
}

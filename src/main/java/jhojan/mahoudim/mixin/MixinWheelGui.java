package jhojan.mahoudim.mixin;

import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import stepsword.mahoutsukai.render.gui.WheelGui;

@Mixin(value = WheelGui.class, remap = false)
public abstract class MixinWheelGui {
   @ModifyConstant(method = "<init>", constant = @Constant(intValue = 3), remap = false)
   private int mahoudim$useSixSectorsForSecondCode(int original, Level level, ItemStack stack) {
      return stack.getItem() instanceof SecondSorceryCode ? SecondSorceryCode.SLOT_COUNT : original;
   }
}

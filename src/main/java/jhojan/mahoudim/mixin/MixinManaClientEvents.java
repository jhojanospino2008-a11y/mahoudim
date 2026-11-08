package jhojan.mahoudim.mixin;

import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stepsword.mahoutsukai.client.ManaClientEvents;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;

/** Lets Mahou Tsukai's existing wheel open for a V2 held in the offhand. */
@Mixin(value = ManaClientEvents.class, remap = false)
public abstract class MixinManaClientEvents {
   @Redirect(
      method = "keyPress",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
         ordinal = 0,
         remap = false
      ),
      remap = false
   )
   private static ItemStack mahoudim$useOffhandSecondCodeForWheel(LocalPlayer player) {
      ItemStack mainHand = player.getMainHandItem();
      if (mainHand.getItem() instanceof MysticCode) {
         return mainHand;
      }

      ItemStack offHand = player.getOffhandItem();
      return offHand.getItem() instanceof SecondSorceryCode ? offHand : mainHand;
   }
}

package jhojan.mahoudim.mixin;

import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;
import stepsword.mahoutsukai.item.spells.secret.FallenDownSpellScroll;
import stepsword.mahoutsukai.item.spells.secret.GandrSpellScroll;
import stepsword.mahoutsukai.networking.MysticCodeSwitchPacket;

@Mixin(
   value = {MysticCodeSwitchPacket.class},
   remap = false
)
public abstract class MixinMysticCodeSwitchPacket {
   @Inject(
      method = {"handleSwitch"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void mahoudim$selectOffhandSecondCode(MysticCodeSwitchPacket packet, Player player, CallbackInfo callback) {
      if (!(player.getMainHandItem().getItem() instanceof MysticCode)) {
         ItemStack offHand = player.getOffhandItem();
         if (offHand.getItem() instanceof SecondSorceryCode code) {
            int selectedSlot = ((MixinMysticCodeSwitchPacketAccessor)packet).mahoudim$getSlot();
            if (selectedSlot >= 0 && selectedSlot < 6) {
               MysticCode.setSlot(offHand, selectedSlot);
               code.setRandomColor(offHand, player.getRandom());
               ItemStack selectedScroll = SecondSorceryCode.inventory(offHand).getStackInSlot(selectedSlot);
               if (selectedScroll.getItem() instanceof GandrSpellScroll) {
                  code.setColor(offHand, 8);
               } else if (selectedScroll.getItem() instanceof FallenDownSpellScroll) {
                  code.setColor(offHand, 9);
               }

               callback.cancel();
            } else {
               callback.cancel();
            }
         }
      }
   }
}

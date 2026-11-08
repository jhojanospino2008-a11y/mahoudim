package jhojan.mahoudim.mixin;

import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents third-person attack posing only when the swinging hand holds the V2 code. */
@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {
   @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
   private void mahoudim$skipSecondCodeAttackPose(LivingEntity entity, float ageInTicks, CallbackInfo callback) {
      InteractionHand swingingHand = entity.swingingArm;
      if (entity instanceof Player player
         && swingingHand != null
         && player.getItemInHand(swingingHand).getItem() instanceof SecondSorceryCode) {
         callback.cancel();
      }
   }
}

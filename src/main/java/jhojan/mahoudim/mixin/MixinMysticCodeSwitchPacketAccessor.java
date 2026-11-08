package jhojan.mahoudim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stepsword.mahoutsukai.networking.MysticCodeSwitchPacket;

@Mixin(value = MysticCodeSwitchPacket.class, remap = false)
public interface MixinMysticCodeSwitchPacketAccessor {
   @Accessor(value = "slot", remap = false)
   int mahoudim$getSlot();
}

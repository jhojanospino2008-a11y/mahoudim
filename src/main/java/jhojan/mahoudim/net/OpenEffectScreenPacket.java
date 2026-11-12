package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenEffectScreenPacket(int kind, int outputMode) implements CustomPacketPayload {
   public static final Type<OpenEffectScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("mahoudim", "open_effect_screen"));
   public static final StreamCodec<ByteBuf, OpenEffectScreenPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, OpenEffectScreenPacket::kind,
      ByteBufCodecs.VAR_INT, OpenEffectScreenPacket::outputMode,
      OpenEffectScreenPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}

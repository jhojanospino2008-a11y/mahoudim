package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenCorePacket() implements CustomPacketPayload {
   public static final Type<OpenCorePacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("mahoudim", "open_core"));
   public static final StreamCodec<ByteBuf, OpenCorePacket> CODEC = StreamCodec.unit(new OpenCorePacket());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}

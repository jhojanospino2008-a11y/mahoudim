package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenGatePacket(BlockPos pos, CompoundTag cfg, boolean inRealm) implements CustomPacketPayload {
   public static final Type<OpenGatePacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("mahoudim", "open_gate"));
   public static final StreamCodec<ByteBuf, OpenGatePacket> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      OpenGatePacket::pos,
      ByteBufCodecs.COMPOUND_TAG,
      OpenGatePacket::cfg,
      ByteBufCodecs.BOOL,
      OpenGatePacket::inRealm,
      OpenGatePacket::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}

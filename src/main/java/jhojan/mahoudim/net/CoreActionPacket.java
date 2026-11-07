package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import jhojan.mahoudim.tech.TechniqueActions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CoreActionPacket(int radius, int seconds, int multiplier) implements CustomPacketPayload {
   public static final Type<CoreActionPacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("mahoudim", "core_action"));
   public static final StreamCodec<ByteBuf, CoreActionPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      CoreActionPacket::radius,
      ByteBufCodecs.VAR_INT,
      CoreActionPacket::seconds,
      ByteBufCodecs.VAR_INT,
      CoreActionPacket::multiplier,
      CoreActionPacket::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CoreActionPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer p) {
         TechniqueActions.activateChronoGarden(
            p, Math.max(3, Math.min(16, msg.radius())), Math.max(1, Math.min(60, msg.seconds())), Math.max(1, Math.min(16, msg.multiplier()))
         );
      }
   }
}

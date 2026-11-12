package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import jhojan.mahoudim.tech.EffectTechniqueActions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EffectActionPacket(int action, ResourceLocation effectId, int level, int durationSeconds, boolean particles, int outputMode)
   implements CustomPacketPayload {
   public static final Type<EffectActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("mahoudim", "effect_action"));
   public static final StreamCodec<ByteBuf, EffectActionPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, EffectActionPacket::action,
      ResourceLocation.STREAM_CODEC, EffectActionPacket::effectId,
      ByteBufCodecs.VAR_INT, EffectActionPacket::level,
      ByteBufCodecs.VAR_INT, EffectActionPacket::durationSeconds,
      ByteBufCodecs.BOOL, EffectActionPacket::particles,
      ByteBufCodecs.VAR_INT, EffectActionPacket::outputMode,
      EffectActionPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(EffectActionPacket message, IPayloadContext context) {
      context.enqueueWork(() -> {
         if (context.player() instanceof ServerPlayer player) {
            EffectTechniqueActions.handle(player, message);
         }
      });
   }
}

package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import jhojan.mahoudim.block.GateCircleBlock;
import jhojan.mahoudim.realm.RealmConfig;
import jhojan.mahoudim.realm.RealmData;
import jhojan.mahoudim.realm.RealmManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GateActionPacket(BlockPos pos, CompoundTag cfg, int action) implements CustomPacketPayload {
   public static final int SAVE = 0;
   public static final int ENTER = 1;
   public static final int RETURN = 2;
   public static final Type<GateActionPacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("mahoudim", "gate_action"));
   public static final StreamCodec<ByteBuf, GateActionPacket> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      GateActionPacket::pos,
      ByteBufCodecs.COMPOUND_TAG,
      GateActionPacket::cfg,
      ByteBufCodecs.VAR_INT,
      GateActionPacket::action,
      GateActionPacket::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(GateActionPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer p) {
         boolean gate = p.distanceToSqr(Vec3.atCenterOf(msg.pos())) <= 64.0 && p.level().getBlockState(msg.pos()).getBlock() instanceof GateCircleBlock;
         boolean remote = p.distanceToSqr(Vec3.atCenterOf(msg.pos())) <= 4.0 && RealmData.root(p).contains("cfg");
         if (gate || remote) {
            RealmConfig cfg = RealmConfig.fromTag(msg.cfg());
            switch (msg.action()) {
               case 0:
                  RealmData.setConfig(p, cfg);
                  break;
               case 1:
                  RealmManager.enter(p, cfg);
                  break;
               case 2:
                  RealmManager.returnHome(p);
            }
         }
      }
   }
}

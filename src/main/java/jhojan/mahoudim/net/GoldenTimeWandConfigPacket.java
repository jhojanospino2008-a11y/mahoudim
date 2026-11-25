package jhojan.mahoudim.net;

import io.netty.buffer.ByteBuf;
import jhojan.mahoudim.item.GoldenTimeWandItem;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GoldenTimeWandConfigPacket(int action) implements CustomPacketPayload {
    public static final Type<GoldenTimeWandConfigPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("mahoudim", "golden_time_wand_config"));
    public static final StreamCodec<ByteBuf, GoldenTimeWandConfigPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GoldenTimeWandConfigPacket::action, GoldenTimeWandConfigPacket::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GoldenTimeWandConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GoldenTimeWandItem)) stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof GoldenTimeWandItem wand)) return;
            if (packet.action() == 0) GoldenTimeWandItem.cycleDuration(stack);
            else GoldenTimeWandItem.cycleTarget(stack);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                packet.action() == 0 ? "mahoudim.golden_time_wand.duration_changed" : "mahoudim.golden_time_wand.target_changed",
                packet.action() == 0 ? GoldenTimeWandItem.durationText(stack) : GoldenTimeWandItem.targetText(stack)
            ), true);
        });
    }
}

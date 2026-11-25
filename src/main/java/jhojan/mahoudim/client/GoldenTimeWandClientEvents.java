package jhojan.mahoudim.client;

import com.mojang.blaze3d.platform.InputConstants;
import jhojan.mahoudim.item.GoldenTimeWandItem;
import jhojan.mahoudim.net.GoldenTimeWandConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "mahoudim", value = Dist.CLIENT)
public final class GoldenTimeWandClientEvents {
    private GoldenTimeWandClientEvents() {}

    @SubscribeEvent
    public static void onKey(Key event) {
        if (event.getAction() != 1 || event.getKey() != InputConstants.KEY_Y) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof GoldenTimeWandItem)) stack = minecraft.player.getOffhandItem();
        if (!(stack.getItem() instanceof GoldenTimeWandItem)) return;
        PacketDistributor.sendToServer(new GoldenTimeWandConfigPacket(minecraft.player.isShiftKeyDown() ? 1 : 0));
    }
}

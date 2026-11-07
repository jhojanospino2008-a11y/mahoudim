package jhojan.mahoudim.client;

import jhojan.mahoudim.net.OpenCorePacket;
import jhojan.mahoudim.net.OpenGatePacket;
import jhojan.mahoudim.realm.RealmConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientHandlers {
   private ClientHandlers() {
   }

   public static void openGate(OpenGatePacket msg, IPayloadContext ctx) {
      Minecraft.getInstance().setScreen(new GateScreen(msg.pos(), RealmConfig.fromTag(msg.cfg()), msg.inRealm()));
   }

   public static void openCore(OpenCorePacket msg, IPayloadContext ctx) {
      Minecraft.getInstance().setScreen(new CoreScreen());
   }
}

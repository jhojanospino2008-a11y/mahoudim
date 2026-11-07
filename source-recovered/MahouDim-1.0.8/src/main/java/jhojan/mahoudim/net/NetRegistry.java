package jhojan.mahoudim.net;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetRegistry {
   private NetRegistry() {
   }

   public static void register(RegisterPayloadHandlersEvent var0) {
      PayloadRegistrar var1 = var0.registrar("1");
      var1.playToClient(OpenGatePacket.TYPE, OpenGatePacket.CODEC, ClientPayloadRouter::openGate);
      var1.playToClient(OpenCorePacket.TYPE, OpenCorePacket.CODEC, ClientPayloadRouter::openCore);
      var1.playToServer(GateActionPacket.TYPE, GateActionPacket.CODEC, GateActionPacket::handle);
      var1.playToServer(CoreActionPacket.TYPE, CoreActionPacket.CODEC, CoreActionPacket::handle);
   }
}

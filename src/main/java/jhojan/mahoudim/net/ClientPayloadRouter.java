package jhojan.mahoudim.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadRouter {
   private static final String CLIENT_HANDLERS = "jhojan.mahoudim.client.ClientHandlers";

   private ClientPayloadRouter() {
   }

   public static void openGate(OpenGatePacket var0, IPayloadContext var1) {
      invoke("openGate", OpenGatePacket.class, var0, var1);
   }

   public static void openCore(OpenCorePacket var0, IPayloadContext var1) {
      invoke("openCore", OpenCorePacket.class, var0, var1);
   }

   public static void openEffectScreen(OpenEffectScreenPacket packet, IPayloadContext context) {
      invoke("openEffectScreen", OpenEffectScreenPacket.class, packet, context);
   }

   private static void invoke(String var0, Class<?> var1, Object var2, IPayloadContext var3) {
      try {
         Class var4 = Class.forName("jhojan.mahoudim.client.ClientHandlers");
         Method var9 = var4.getMethod(var0, var1, IPayloadContext.class);
         var9.invoke(null, var2, var3);
      } catch (ClassNotFoundException var6) {
      } catch (InvocationTargetException var7) {
         Throwable var5 = var7.getCause();
         if (var5 instanceof RuntimeException) {
            throw (RuntimeException)var5;
         }

         if (var5 instanceof Error) {
            throw (Error)var5;
         }

         throw new IllegalStateException("Client payload handler failed: " + var0, var5);
      } catch (ReflectiveOperationException var8) {
         throw new IllegalStateException("Unable to dispatch client payload: " + var0, var8);
      }
   }
}

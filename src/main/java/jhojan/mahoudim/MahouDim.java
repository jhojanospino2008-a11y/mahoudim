package jhojan.mahoudim;

import com.mojang.logging.LogUtils;
import jhojan.mahoudim.client.ClientSetup;
import jhojan.mahoudim.item.JuadanaEvents;
import jhojan.mahoudim.item.SecondCodeCraftingEvents;
import jhojan.mahoudim.net.NetRegistry;
import jhojan.mahoudim.realm.RealmEvents;
import jhojan.mahoudim.tech.MahoraAdaptationEvents;
import jhojan.mahoudim.tech.TechniqueEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("mahoudim")
public class MahouDim {
   public static final String MODID = "mahoudim";
   public static final Logger LOGGER = LogUtils.getLogger();

   public MahouDim(IEventBus bus, ModContainer container) {
      Reg.BLOCKS.register(bus);
      Reg.ITEMS.register(bus);
      Reg.BLOCK_ENTITIES.register(bus);
      Reg.MENUS.register(bus);
      Reg.FEATURES.register(bus);
      Reg.TABS.register(bus);
      container.registerConfig(Type.SERVER, Config.SPEC);
      bus.addListener(this::commonSetup);
      bus.addListener(this::loadComplete);
      bus.addListener(NetRegistry::register);
      NeoForge.EVENT_BUS.register(RealmEvents.class);
      NeoForge.EVENT_BUS.register(TechniqueEvents.class);
      NeoForge.EVENT_BUS.register(MahoraAdaptationEvents.class);
      NeoForge.EVENT_BUS.register(JuadanaEvents.class);
      NeoForge.EVENT_BUS.register(SecondCodeCraftingEvents.class);
      if (FMLEnvironment.dist == Dist.CLIENT) {
         ClientSetup.init(bus);
      }
   }

   private void commonSetup(FMLCommonSetupEvent event) {
   }

   private void loadComplete(FMLLoadCompleteEvent event) {
      RecipeHook.register();
      PowderCatalog.register();
   }
}

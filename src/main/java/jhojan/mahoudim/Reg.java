package jhojan.mahoudim;

import java.util.EnumMap;
import java.util.Map;
import jhojan.mahoudim.block.GateCircleBlock;
import jhojan.mahoudim.block.PocketCircleBlock;
import jhojan.mahoudim.block.TechniqueCircleBlock;
import jhojan.mahoudim.item.JuadanaItem;
import jhojan.mahoudim.item.MahoraWheelItem;
import jhojan.mahoudim.item.SecondSorceryCode;
import jhojan.mahoudim.item.TechniqueScroll;
import jhojan.mahoudim.menu.SecondCodeMenu;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tile.TechniqueCircleTile;
import jhojan.mahoudim.tile.UtilityCircleTile;
import jhojan.mahoudim.worldgen.TaggedOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import stepsword.mahoutsukai.item.catalysts.PowderedCatalyst;

public final class Reg {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "mahoudim");
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "mahoudim");
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "mahoudim");
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "mahoudim");
   public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "mahoudim");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "mahoudim");
   public static final DeferredHolder<Feature<?>, TaggedOreFeature> TAGGED_ORE = FEATURES.register("tagged_ore", TaggedOreFeature::new);
   public static final DeferredHolder<Block, GateCircleBlock> GATE = BLOCKS.register("mahoujin_dimensional_gate", GateCircleBlock::new);
   public static final DeferredHolder<Block, PocketCircleBlock> POCKET = BLOCKS.register("mahoujin_spatial_pocket", PocketCircleBlock::new);
   public static final Map<Technique.Cat, DeferredHolder<Item, PowderedCatalyst>> POWDERS = new EnumMap<>(Technique.Cat.class);
   public static final Map<Technique, DeferredHolder<Block, TechniqueCircleBlock>> TECH_BLOCKS = new EnumMap<>(Technique.class);
   public static final Map<Technique, DeferredHolder<Item, TechniqueScroll>> TECH_SCROLLS = new EnumMap<>(Technique.class);
   public static final DeferredHolder<Item, MahoraWheelItem> MAHORA_WHEEL = ITEMS.register("mahora_wheel", MahoraWheelItem::new);
   public static final DeferredHolder<Item, JuadanaItem> JUADANA = ITEMS.register("juadana", JuadanaItem::new);
   public static final DeferredHolder<Item, SecondSorceryCode> SECOND_SORCERY_CODE = ITEMS.register("second_sorcery_code", SecondSorceryCode::new);
   public static final DeferredHolder<MenuType<?>, MenuType<SecondCodeMenu>> SECOND_CODE_MENU = MENUS.register(
      "second_sorcery_code", () -> IMenuTypeExtension.create(SecondCodeMenu::new)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UtilityCircleTile>> UTILITY_TILE;
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TechniqueCircleTile>> TECHNIQUE_TILE;
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB;

   private static void registerPowder(Technique.Cat cat, String id) {
      POWDERS.put(cat, ITEMS.register(id, () -> new PowderedCatalyst("mahoudim:" + id)));
   }

   private Reg() {
   }

   static {
      registerPowder(Technique.Cat.STONE, "powdered_stone");
      registerPowder(Technique.Cat.END_STONE, "powdered_end_stone");
      registerPowder(Technique.Cat.OBSIDIAN, "powdered_obsidian");
      registerPowder(Technique.Cat.AMETHYST, "powdered_amethyst");
      registerPowder(Technique.Cat.PRISMARINE, "powdered_prismarine");
      registerPowder(Technique.Cat.SCULK, "powdered_sculk");
      registerPowder(Technique.Cat.COPPER, "powdered_copper");
      registerPowder(Technique.Cat.NETHERITE, "powdered_netherite");
      registerPowder(Technique.Cat.MAHORA, "powdered_mahora");

      for (Technique t : Technique.values()) {
         TECH_BLOCKS.put(t, BLOCKS.register("mahoujin_" + t.id, () -> new TechniqueCircleBlock(t)));
         TECH_SCROLLS.put(t, ITEMS.register("scroll_" + t.id, () -> new TechniqueScroll(t)));
      }

      UTILITY_TILE = BLOCK_ENTITIES.register(
         "utility_circle", () -> Builder.of(UtilityCircleTile::new, new Block[]{(Block)GATE.get(), (Block)POCKET.get()}).build(null)
      );
      TECHNIQUE_TILE = BLOCK_ENTITIES.register(
         "technique_circle",
         () -> Builder.of(TechniqueCircleTile::new, TECH_BLOCKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null)
      );
      TAB = TABS.register(
         "arcana",
         () -> CreativeModeTab.builder()
               .title(Component.translatable("itemGroup.mahoudim"))
               .icon(() -> new ItemStack((ItemLike)TECH_SCROLLS.get(Technique.TIME_STASIS).get()))
               .displayItems((params, output) -> {
                  for (Technique tx : Technique.values()) {
                     output.accept((ItemLike)TECH_SCROLLS.get(tx).get());
                  }

                  for (DeferredHolder<Item, PowderedCatalyst> powder : POWDERS.values()) {
                     output.accept((ItemLike)powder.get());
                  }

                  output.accept(MAHORA_WHEEL.get());
                  output.accept(JUADANA.get());
                  output.accept(SECOND_SORCERY_CODE.get());
               })
               .build()
      );
   }
}

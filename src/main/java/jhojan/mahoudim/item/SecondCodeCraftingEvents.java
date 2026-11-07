package jhojan.mahoudim.item;

import jhojan.mahoudim.Reg;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import stepsword.mahoutsukai.item.mysticcode.MysticCodeFirstSorcery;

/** Carries over the old Code's three scrolls when the JEI-visible upgrade recipe is crafted. */
public final class SecondCodeCraftingEvents {
   private SecondCodeCraftingEvents() {
   }

   @SubscribeEvent
   public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
      ItemStack result = event.getCrafting();
      if (!result.is(Reg.SECOND_SORCERY_CODE.get())) {
         return;
      }

      Container grid = event.getInventory();
      for (int i = 0; i < grid.getContainerSize(); i++) {
         ItemStack ingredient = grid.getItem(i);
         if (ingredient.getItem() instanceof MysticCodeFirstSorcery) {
            ComponentItemHandler oldInventory = new ComponentItemHandler(ingredient, DataComponents.CONTAINER, 3);
            ComponentItemHandler newInventory = SecondSorceryCode.inventory(result);
            for (int slot = 0; slot < 3; slot++) {
               ItemStack scroll = oldInventory.getStackInSlot(slot);
               if (!scroll.isEmpty()) {
                  newInventory.setStackInSlot(slot, scroll.copy());
               }
            }
            return;
         }
      }
   }
}

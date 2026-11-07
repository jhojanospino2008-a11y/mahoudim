package jhojan.mahoudim.menu;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.item.SecondSorceryCode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import stepsword.mahoutsukai.item.mysticcode.MysticCode;
import stepsword.mahoutsukai.item.spells.SpellScroll;

public class SecondCodeMenu extends AbstractContainerMenu {
   private final ComponentItemHandler codeInventory;
   private final Inventory playerInventory;
   private final ItemStack codeStack;

   public SecondCodeMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
      this(id, playerInventory, heldCode(playerInventory));
   }

   public SecondCodeMenu(int id, Inventory playerInventory, ItemStack codeStack) {
      super(Reg.SECOND_CODE_MENU.get(), id);
      this.playerInventory = playerInventory;
      this.codeStack = codeStack;
      this.codeInventory = SecondSorceryCode.inventory(codeStack);

      for (int i = 0; i < SecondSorceryCode.SLOT_COUNT; i++) {
         int slot = i;
         int x = 44 + (i % 3) * 36;
         int y = 20 + (i / 3) * 20;
         this.addSlot(new SlotItemHandler(this.codeInventory, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
               return stack.getItem() instanceof SpellScroll && !this.hasItem();
            }
         });
      }

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
         }
      }
      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
      }
   }

   private static ItemStack heldCode(Inventory inventory) {
      Player player = inventory.player;
      return player.getMainHandItem().getItem() instanceof SecondSorceryCode
         ? player.getMainHandItem() : player.getOffhandItem();
   }

   @Override
   public boolean stillValid(Player player) {
      return player.getMainHandItem().getItem() instanceof SecondSorceryCode
         || player.getOffhandItem().getItem() instanceof SecondSorceryCode;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      if (index < 0 || index >= this.slots.size()) {
         return ItemStack.EMPTY;
      }
      Slot slot = this.slots.get(index);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      }
      ItemStack source = slot.getItem();
      ItemStack original = source.copy();
      int codeSlots = this.codeInventory.getSlots();
      if (index < codeSlots) {
         if (!this.moveItemStackTo(source, codeSlots, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }
      } else if (source.getItem() instanceof SpellScroll) {
         if (!this.moveItemStackTo(source, 0, codeSlots, false)) {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
      if (source.isEmpty()) {
         slot.set(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }
      return original;
   }
}

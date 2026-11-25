package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class MahoraWheelItem extends Item implements Equipable {
   public MahoraWheelItem() {
      super(new Properties().stacksTo(1).durability(363));
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   public boolean isEnchantable(ItemStack stack) {
      return stack.getDamageValue() == 0;
   }

   public int getEnchantmentValue() {
      return 15;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return this.swapWithEquipmentSlot(this, level, player, hand);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.mahoudim.mahora_wheel.tooltip"));
      CompoundTag data = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      if (data.contains("mahora_adapted_types", 9)) {
         ListTag types = data.getList("mahora_adapted_types", 8);

         for (int i = 0; i < types.size(); i++) {
            tooltip.add(Component.translatable("item.mahoudim.mahora_wheel.adapted_to", new Object[]{types.getString(i)}));
         }
      }
   }
}

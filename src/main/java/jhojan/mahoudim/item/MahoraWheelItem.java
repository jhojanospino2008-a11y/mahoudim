package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class MahoraWheelItem extends ArmorItem {
   public MahoraWheelItem() {
      super(ArmorMaterials.IRON, Type.HELMET, new Properties().stacksTo(1));
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.mahoudim.mahora_wheel.tooltip"));
   }
}

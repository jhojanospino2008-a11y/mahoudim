package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MahoraWheelItem extends Item implements Equipable {
   public MahoraWheelItem() {
      super(new Properties().stacksTo(1));
   }

   @Override
   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      return swapWithEquipmentSlot(this, level, player, hand);
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.mahoudim.mahora_wheel.tooltip"));
   }
}

package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;

public class JuadanaItem extends SwordItem {
   public static final int MAX_CHARGE = 100;
   private static final String CHARGE_KEY = "mahoudim_death_charge";

   public JuadanaItem() {
      super(Tiers.NETHERITE, new Properties().stacksTo(1).durability(2031).attributes(SwordItem.createAttributes(Tiers.NETHERITE, 15, -2.4F)));
   }

   public static int getCharge(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? 0 : Math.max(0, Math.min(100, data.copyTag().getInt("mahoudim_death_charge")));
   }

   public static void setCharge(ItemStack stack, int charge) {
      int safe = Math.max(0, Math.min(100, charge));
      CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("mahoudim_death_charge", safe));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.juadana.tooltip"));
   }
}

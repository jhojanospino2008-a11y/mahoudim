package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

public class JuadanaItem extends SwordItem {
   public static final int MAX_CHARGE = 100;
   private static final String CHARGE_KEY = "mahoudim_death_charge";

   public JuadanaItem() {
      // Netheritic tier contributes 4 damage; 15 + 4 + the player's base 1 = 20.
      super(Tiers.NETHERITE, new Item.Properties()
         .stacksTo(1)
         .durability(2031)
         .attributes(SwordItem.createAttributes((Tier)Tiers.NETHERITE, 15, -2.4F)));
   }

   public static int getCharge(net.minecraft.world.item.ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? 0 : Math.max(0, Math.min(MAX_CHARGE, data.copyTag().getInt(CHARGE_KEY)));
   }

   public static void setCharge(net.minecraft.world.item.ItemStack stack, int charge) {
      int safe = Math.max(0, Math.min(MAX_CHARGE, charge));
      CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(CHARGE_KEY, safe));
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.mahoudim.juadana.tooltip"));
   }
}

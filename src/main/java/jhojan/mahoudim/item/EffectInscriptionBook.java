package jhojan.mahoudim.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.alchemy.PotionContents;

public class EffectInscriptionBook extends Item {
   public EffectInscriptionBook() {
      super(new Properties().stacksTo(1));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
      if (contents != null && contents.hasEffects()) {
         contents.addPotionTooltip(tooltip::add, 1.0F, 20.0F);
      }

      tooltip.add(Component.translatable("item.mahoudim.effect_inscription_book.apply_hint").withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.translatable("item.mahoudim.effect_inscription_book.while_held").withStyle(ChatFormatting.DARK_GRAY));
   }
}

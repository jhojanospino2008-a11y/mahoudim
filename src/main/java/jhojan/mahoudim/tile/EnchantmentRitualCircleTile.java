package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.EnchantmentRitualCircleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;

public final class EnchantmentRitualCircleTile extends MahoujinTileEntity implements ScrollGiver {
   public EnchantmentRitualCircleTile(BlockPos pos, BlockState state) {
      super((BlockEntityType)Reg.ENCHANTMENT_RITUAL_TILE.get(), pos, state);
   }

   public ItemStack getItemToGive() {
      return this.getBlockState().getBlock() instanceof EnchantmentRitualCircleBlock circle
         ? new ItemStack((ItemLike)Reg.ENCHANTMENT_SCROLLS.get(circle.enchantment).get())
         : ItemStack.EMPTY;
   }
}

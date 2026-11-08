package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.EnchantmentRitualCircleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;

public final class EnchantmentRitualCircleTile extends MahoujinTileEntity implements ScrollGiver {
   public EnchantmentRitualCircleTile(BlockPos pos, BlockState state) {
      super(Reg.ENCHANTMENT_RITUAL_TILE.get(), pos, state);
   }

   @Override
   public ItemStack getItemToGive() {
      if (!(this.getBlockState().getBlock() instanceof EnchantmentRitualCircleBlock circle)) {
         return ItemStack.EMPTY;
      }
      return new ItemStack(Reg.ENCHANTMENT_SCROLLS.get(circle.enchantment).get());
   }
}

package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.TechniqueCircleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;

public class TechniqueCircleTile extends MahoujinTileEntity implements ScrollGiver {
   public TechniqueCircleTile(BlockPos pos, BlockState state) {
      super((BlockEntityType)Reg.TECHNIQUE_TILE.get(), pos, state);
   }

   public ItemStack getItemToGive() {
      return this.getBlockState().getBlock() instanceof TechniqueCircleBlock tcb
         ? new ItemStack((ItemLike)Reg.TECH_SCROLLS.get(tcb.technique).get())
         : ItemStack.EMPTY;
   }
}

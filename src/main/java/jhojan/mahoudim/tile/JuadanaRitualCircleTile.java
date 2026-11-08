package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;

public final class JuadanaRitualCircleTile extends MahoujinTileEntity implements ScrollGiver {
   public JuadanaRitualCircleTile(BlockPos pos, BlockState state) {
      super((BlockEntityType)Reg.JUADANA_RITUAL_TILE.get(), pos, state);
   }

   @Override
   public ItemStack getItemToGive() {
      return new ItemStack(Reg.JUADANA_RITUAL_SCROLL.get());
   }
}

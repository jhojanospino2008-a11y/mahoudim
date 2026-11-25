package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;

public class UtilityCircleTile extends MahoujinTileEntity {
   public UtilityCircleTile(BlockPos pos, BlockState state) {
      super((BlockEntityType)Reg.UTILITY_TILE.get(), pos, state);
   }

   public boolean isEnabled() {
      return !this.getPersistentData().contains("mahoudim_enabled") || this.getPersistentData().getBoolean("mahoudim_enabled");
   }

   public boolean toggleEnabled() {
      boolean next = !this.isEnabled();
      this.getPersistentData().putBoolean("mahoudim_enabled", next);
      this.setCloth(next);
      this.sendUpdates();
      this.setChanged();
      return next;
   }

   public void activateVisual() {
      this.setCloth(true);
      this.sendUpdates();
   }
}

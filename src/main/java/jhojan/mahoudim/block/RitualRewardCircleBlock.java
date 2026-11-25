package jhojan.mahoudim.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import stepsword.mahoutsukai.block.spells.SingleUseMahoujinBlockTileEntity;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;

public abstract class RitualRewardCircleBlock<T extends MahoujinTileEntity> extends SingleUseMahoujinBlockTileEntity<T> {
   protected RitualRewardCircleBlock(String name) {
      super(name);
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide && level.getBlockEntity(pos) instanceof MahoujinTileEntity tile) {
         tile.setCloth(true);
      }

      return super.useWithoutItem(state, level, pos, player, hit);
   }
}

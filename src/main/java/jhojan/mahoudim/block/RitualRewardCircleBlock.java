package jhojan.mahoudim.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import stepsword.mahoutsukai.block.spells.SingleUseMahoujinBlockTileEntity;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;

/** One-use result circle; its ritual passed the carpet-layout check and grants a SpellScroll. */
public abstract class RitualRewardCircleBlock<T extends MahoujinTileEntity>
   extends SingleUseMahoujinBlockTileEntity<T> {
   protected RitualRewardCircleBlock(String name) {
      super(name);
   }

   @Override
   public InteractionResult useWithoutItem(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      BlockHitResult hit
   ) {
      if (!level.isClientSide) {
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (blockEntity instanceof MahoujinTileEntity tile) {
            // SingleUseMahoujinBlockTileEntity only emits its reward when this flag is set.
            tile.setCloth(true);
         }
      }
      return super.useWithoutItem(state, level, pos, player, hit);
   }
}

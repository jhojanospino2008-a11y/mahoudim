package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.tile.JuadanaRitualCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.block.spells.SingleUseMahoujinBlockTileEntity;

/** One-shot Mahoujin result block that yields the Juadana ritual scroll. */
public final class JuadanaRitualCircleBlock extends SingleUseMahoujinBlockTileEntity<JuadanaRitualCircleTile> {
   public JuadanaRitualCircleBlock() {
      super("mahoujin_juadana_ritual");
   }

   @Override
   public Class<JuadanaRitualCircleTile> getTileEntityClass() {
      return JuadanaRitualCircleTile.class;
   }

   @Nullable
   @Override
   public JuadanaRitualCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new JuadanaRitualCircleTile(pos, state);
   }
}

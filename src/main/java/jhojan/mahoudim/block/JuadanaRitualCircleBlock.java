package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.tile.JuadanaRitualCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class JuadanaRitualCircleBlock extends RitualRewardCircleBlock<JuadanaRitualCircleTile> {
   public JuadanaRitualCircleBlock() {
      super("mahoujin_juadana_ritual");
   }

   public Class<JuadanaRitualCircleTile> getTileEntityClass() {
      return JuadanaRitualCircleTile.class;
   }

   @Nullable
   public JuadanaRitualCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new JuadanaRitualCircleTile(pos, state);
   }
}

package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tile.TechniqueCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.block.spells.SingleUseMahoujinBlockTileEntity;

public class TechniqueCircleBlock extends SingleUseMahoujinBlockTileEntity<TechniqueCircleTile> {
   public final Technique technique;

   public TechniqueCircleBlock(Technique technique) {
      super("mahoujin_" + technique.id);
      this.technique = technique;
   }

   public Class<TechniqueCircleTile> getTileEntityClass() {
      return TechniqueCircleTile.class;
   }

   @Nullable
   public TechniqueCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new TechniqueCircleTile(pos, state);
   }
}

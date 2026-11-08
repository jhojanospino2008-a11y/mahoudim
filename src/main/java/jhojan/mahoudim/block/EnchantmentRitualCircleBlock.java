package jhojan.mahoudim.block;

import jhojan.mahoudim.enchantment.RitualEnchantment;
import jhojan.mahoudim.tile.EnchantmentRitualCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** One-shot Mahoujin result block that yields its matching ritual scroll. */
public final class EnchantmentRitualCircleBlock extends RitualRewardCircleBlock<EnchantmentRitualCircleTile> {
   public final RitualEnchantment enchantment;

   public EnchantmentRitualCircleBlock(RitualEnchantment enchantment) {
      super(enchantment.blockId());
      this.enchantment = enchantment;
   }

   @Override
   public Class<EnchantmentRitualCircleTile> getTileEntityClass() {
      return EnchantmentRitualCircleTile.class;
   }

   @Nullable
   @Override
   public EnchantmentRitualCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new EnchantmentRitualCircleTile(pos, state);
   }
}

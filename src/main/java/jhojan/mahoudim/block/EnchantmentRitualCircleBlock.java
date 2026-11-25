package jhojan.mahoudim.block;

import jhojan.mahoudim.enchantment.RitualEnchantment;
import jhojan.mahoudim.tile.EnchantmentRitualCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentRitualCircleBlock extends RitualRewardCircleBlock<EnchantmentRitualCircleTile> {
   public final RitualEnchantment enchantment;

   public EnchantmentRitualCircleBlock(RitualEnchantment enchantment) {
      super(enchantment.blockId());
      this.enchantment = enchantment;
   }

   public Class<EnchantmentRitualCircleTile> getTileEntityClass() {
      return EnchantmentRitualCircleTile.class;
   }

   @Nullable
   public EnchantmentRitualCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new EnchantmentRitualCircleTile(pos, state);
   }
}

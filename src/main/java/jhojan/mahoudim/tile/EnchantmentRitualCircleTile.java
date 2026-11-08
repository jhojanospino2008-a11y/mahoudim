package jhojan.mahoudim.tile;

import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.EnchantmentRitualCircleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;

public final class EnchantmentRitualCircleTile extends MahoujinTileEntity implements ScrollGiver {
   public EnchantmentRitualCircleTile(BlockPos pos, BlockState state) {
      super(Reg.ENCHANTMENT_RITUAL_TILE.get(), pos, state);
   }

   @Override
   public ItemStack getItemToGive() {
      if (this.level == null || !(this.getBlockState().getBlock() instanceof EnchantmentRitualCircleBlock circle)) {
         return ItemStack.EMPTY;
      }
      Registry<Enchantment> registry = this.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
      ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("mahoudim", circle.enchantment.id));
      Holder<Enchantment> holder = registry.getHolder(key).orElse(null);
      return holder == null ? ItemStack.EMPTY : EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, 1));
   }
}

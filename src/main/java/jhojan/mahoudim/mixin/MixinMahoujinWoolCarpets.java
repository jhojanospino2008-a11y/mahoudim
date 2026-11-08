package jhojan.mahoudim.mixin;

import jhojan.mahoudim.RecipeHook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stepsword.mahoutsukai.block.spells.Mahoujin;
import stepsword.mahoutsukai.dataattachments.mahou.IMahou;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;
import stepsword.mahoutsukai.util.UnorderedList;
import stepsword.mahoutsukai.util.Utils;

/** Requires a four-carpet physical layout for MahouDim's own ritual recipes. */
@Mixin(value = Mahoujin.class, remap = false)
public abstract class MixinMahoujinWoolCarpets {
   @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true, remap = false)
   private void mahoudim$requireCarpetLayout(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir
   ) {
      if (!level.isClientSide && mahoudim$isCarpetRitual(player) && !mahoudim$hasCarpetLayout(level, pos)) {
         cir.setReturnValue(InteractionResult.FAIL);
      }
   }

   @Inject(
      method = "useWithoutItem",
      at = @At(
         value = "INVOKE",
         target = "Lstepsword/mahoutsukai/block/spells/Mahoujin;transformToSpell(Lstepsword/mahoutsukai/tile/MahoujinTileEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)V",
         shift = At.Shift.BEFORE
      ),
      remap = false
   )
   private void mahoudim$markCompletedRitualAsClothed(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir
   ) {
      if (level.isClientSide || !mahoudim$isCarpetRitual(player) || !mahoudim$hasCarpetLayout(level, pos)) {
         return;
      }

      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof MahoujinTileEntity tile) {
         tile.setCloth(true);
      }
   }

   @Unique
   private static boolean mahoudim$isCarpetRitual(Player player) {
      IMahou mahou = Utils.getPlayerMahou(player);
      if (mahou == null) {
         return false;
      }
      UnorderedList lastRecipe = mahou.getLastRecipe();
      return RecipeHook.isCarpetRitual(lastRecipe);
   }

   @Unique
   private static boolean mahoudim$hasCarpetLayout(Level level, BlockPos center) {
      return mahoudim$isWoolCarpet(level, center.relative(Direction.NORTH))
         && mahoudim$isWoolCarpet(level, center.relative(Direction.SOUTH))
         && mahoudim$isWoolCarpet(level, center.relative(Direction.EAST))
         && mahoudim$isWoolCarpet(level, center.relative(Direction.WEST));
   }

   @Unique
   private static boolean mahoudim$isWoolCarpet(Level level, BlockPos pos) {
      return level.getBlockState(pos).is(BlockTags.WOOL_CARPETS);
   }
}

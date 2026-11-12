package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.TechniqueActions;
import jhojan.mahoudim.tile.TechniqueCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import stepsword.mahoutsukai.block.spells.SingleUseMahoujinBlockTileEntity;
import stepsword.mahoutsukai.tile.MahoujinTileEntity;

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

   @Override
   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      boolean perimeter = this.technique == Technique.CHRONO_GARDEN || this.technique == Technique.TEMPORAL_CORE;
      if (!perimeter) {
         if (!level.isClientSide && level.getBlockEntity(pos) instanceof MahoujinTileEntity tile && !tile.hasCloth()) {
            // Spell cloth is optional for these recipes; set the base reward gate only when claiming it.
            tile.setCloth(true);
         }
         return super.useWithoutItem(state, level, pos, player, hit);
      }

      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      if (!(level.getBlockEntity(pos) instanceof TechniqueCircleTile tile)) {
         return InteractionResult.FAIL;
      }
      if (!tile.canControl(player)) {
         player.displayClientMessage(Component.translatable("mahoudim.msg.perimeter_owner"), true);
         return InteractionResult.FAIL;
      }
      if (tile.getCasterUUID() == null) {
         tile.setCaster(player);
      }
      tile.claimScroll(player);

      if (this.technique == Technique.TEMPORAL_CORE && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
         TechniqueActions.openCoreAt(serverPlayer, pos);
      } else {
         tile.toggleBy(player);
         String key = tile.getSwitchOn() ? "mahoudim.msg.perimeter_on" : "mahoudim.msg.perimeter_off";
         player.displayClientMessage(Component.translatable(key, tile.getManaCost()), true);
      }
      return InteractionResult.SUCCESS;
   }
}

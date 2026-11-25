package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.Config;
import jhojan.mahoudim.tile.UtilityCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import stepsword.mahoutsukai.block.spells.MahoujinBlockTileEntity;
import stepsword.mahoutsukai.mana.PlayerManaManager;

public class PocketCircleBlock extends MahoujinBlockTileEntity<UtilityCircleTile> {
   public static final int BASE_COST = 40;

   public PocketCircleBlock() {
      super("mahoujin_spatial_pocket");
   }

   public Class<UtilityCircleTile> getTileEntityClass() {
      return UtilityCircleTile.class;
   }

   @Nullable
   public UtilityCircleTile newBlockEntity(BlockPos pos, BlockState state) {
      return new UtilityCircleTile(pos, state);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (!level.isClientSide && player instanceof ServerPlayer sp) {
         if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof UtilityCircleTile tile) {
            boolean enabled = tile.toggleEnabled();
            sp.displayClientMessage(Component.translatable(enabled ? "mahoudim.msg.circle_enabled" : "mahoudim.msg.circle_disabled"), true);
            return InteractionResult.CONSUME;
         }

         if (level.getBlockEntity(pos) instanceof UtilityCircleTile tile && !tile.isEnabled()) {
            sp.displayClientMessage(Component.translatable("mahoudim.msg.circle_disabled"), true);
            return InteractionResult.CONSUME;
         }

         if (level.getBlockEntity(pos) instanceof UtilityCircleTile tile) {
            tile.activateVisual();
         }

         int cost = Config.scaled(40);
         if (PlayerManaManager.drainMana(sp, cost, false, false) != cost) {
            sp.displayClientMessage(Component.translatable("mahoudim.msg.no_mana", new Object[]{cost}), true);
         } else {
            sp.openMenu(
               new SimpleMenuProvider(
                  (id, inv, pl) -> ChestMenu.threeRows(id, inv, pl.getEnderChestInventory()), Component.translatable("container.enderchest")
               )
            );
         }
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }
}

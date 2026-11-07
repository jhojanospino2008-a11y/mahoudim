package jhojan.mahoudim.block;

import javax.annotation.Nullable;
import jhojan.mahoudim.net.OpenGatePacket;
import jhojan.mahoudim.realm.RealmData;
import jhojan.mahoudim.realm.RealmManager;
import jhojan.mahoudim.tile.UtilityCircleTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import stepsword.mahoutsukai.block.spells.MahoujinBlockTileEntity;

public class GateCircleBlock extends MahoujinBlockTileEntity<UtilityCircleTile> {
   public GateCircleBlock() {
      super("mahoujin_dimensional_gate");
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

         if (level.getBlockEntity(pos) instanceof UtilityCircleTile tile && tile.isEnabled()) {
            tile.activateVisual();
         }

         PacketDistributor.sendToPlayer(sp, new OpenGatePacket(pos, RealmData.config(sp).toTag(), RealmManager.inRealm(level)), new CustomPacketPayload[0]);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (!level.isClientSide && entity instanceof ServerPlayer sp) {
         if (level.getBlockEntity(pos) instanceof UtilityCircleTile tile && !tile.isEnabled()) {
            return;
         }

         if (level.getBlockEntity(pos) instanceof UtilityCircleTile tile) {
            tile.activateVisual();
         }

         long now = level.getGameTime();
         if (sp.getPersistentData().getLong("mahoudim_gate_lock") <= now) {
            sp.getPersistentData().putLong("mahoudim_gate_lock", now + 10L);
            if (RealmManager.inRealm(level)) {
               RealmManager.returnHome(sp);
            } else {
               RealmManager.enter(sp, RealmData.config(sp));
            }
         }
      }
   }
}

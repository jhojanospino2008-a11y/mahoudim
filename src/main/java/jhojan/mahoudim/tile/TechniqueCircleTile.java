package jhojan.mahoudim.tile;

import java.util.List;
import jhojan.mahoudim.Reg;
import jhojan.mahoudim.block.TechniqueCircleBlock;
import jhojan.mahoudim.tech.Technique;
import jhojan.mahoudim.tech.TechniqueActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import stepsword.mahoutsukai.datacomponents.scrollmahou.ScrollMahou;
import stepsword.mahoutsukai.tile.MahoujinTickingTileEntity;
import stepsword.mahoutsukai.tile.ScrollGiver;
import stepsword.mahoutsukai.tile.boundary.BoundaryMahoujinTileEntity;
import stepsword.mahoutsukai.util.Utils;

/** Mahoujin reward circle, with persistent powered fields for Chrono Garden/Core. */
public class TechniqueCircleTile extends BoundaryMahoujinTileEntity implements ScrollGiver {
   private static final String SCROLL_CLAIMED = "mahoudim_scroll_claimed";
   private static final String FIELD_RADIUS = "mahoudim_field_radius";
   private static final String FIELD_MULTIPLIER = "mahoudim_field_multiplier";
   private static final String FIELD_DURATION = "mahoudim_field_duration";
   private static final String FIELD_EXPIRES = "mahoudim_field_expires";

   private boolean scrollClaimed;
   private int fieldRadius = 10;
   private int fieldMultiplier = 1;
   private int fieldDurationSeconds = 20;
   private long fieldExpiresAt;

   public TechniqueCircleTile(BlockPos pos, BlockState state) {
      super((BlockEntityType)Reg.TECHNIQUE_TILE.get(), true, pos, state);
   }

   @Override
   public int getManaCost() {
      return this.isPerimeterTechnique() ? 5 : 0;
   }

   @Override
   public int getManaCycle() {
      return 1;
   }

   @Override
   public int getBarrierCycle() {
      return 20;
   }

   @Override
   public int getBarrierRadius() {
      return this.isPerimeterTechnique() ? this.fieldRadius : super.getBarrierRadius();
   }

   @Override
   public void doBarrier(List<LivingEntity> entities) {
      if (this.isPerimeterTechnique() && this.level instanceof ServerLevel serverLevel) {
         TechniqueActions.tickGrowthBlocks(serverLevel, this.worldPosition, this.fieldRadius, this.fieldMultiplier);
      }
   }

   @Override
   public void tick(Level level, BlockPos pos, BlockState state, MahoujinTickingTileEntity blockEntity) {
      super.tick(level, pos, state, blockEntity);
      if (!level.isClientSide && this.isPerimeterTechnique() && this.fieldExpiresAt > 0L
         && level.getGameTime() >= this.fieldExpiresAt && this.getSwitchOn()) {
         Player caster = this.getCaster();
         if (caster != null) {
            this.toggleBy(caster);
         }
      }
   }

   @Override
   public ItemStack getItemToGive() {
      Technique technique = this.technique();
      return this.scrollClaimed || technique == null
         ? ItemStack.EMPTY
         : new ItemStack((ItemLike)Reg.TECH_SCROLLS.get(technique).get());
   }

   public boolean isFor(Technique expected) {
      return this.technique() == expected;
   }

   public boolean canControl(Player player) {
      return this.getCasterUUID() == null
         || this.getCasterUUID().equals(player.getUUID())
         || this.isFay()
         || player.isCreative();
   }

   public boolean isPerimeterTechnique() {
      Technique technique = this.technique();
      return technique == Technique.CHRONO_GARDEN || technique == Technique.TEMPORAL_CORE;
   }

   /** Claim the associated scroll once while leaving the active perimeter in place. */
   public boolean claimScroll(Player player) {
      ItemStack scroll = this.getItemToGive();
      if (scroll.isEmpty()) {
         return false;
      }

      ScrollMahou scrollMahou = Utils.getScrollMahou(scroll);
      if (scrollMahou != null) {
         scrollMahou.setCasterUUID(player.getUUID());
         scrollMahou.setCasterName(player.getDisplayName().getString());
         Utils.setScrollMahou(scroll, scrollMahou);
      }
      this.scrollClaimed = true;
      if (!player.getInventory().add(scroll)) {
         player.drop(scroll, false);
      }
      this.sendUpdates();
      return true;
   }

   /** Toggle a Garden perimeter; the Core menu uses configureCore instead. */
   public boolean toggleBy(Player player) {
      if (!this.canControl(player)) {
         return false;
      }
      if (this.getCasterUUID() == null) {
         this.setCaster(player);
      }

      boolean before = this.getSwitchOn();
      super.toggle(player);
      boolean active = this.getSwitchOn();
      if (active != before) {
         this.fieldExpiresAt = active && this.level != null
            ? this.level.getGameTime() + (long)this.fieldDurationSeconds * 20L
            : 0L;
         this.setChanged();
         this.sendUpdates();
      }
      return active != before;
   }

   public boolean configureCore(ServerPlayer player, int radius, int seconds, int multiplier) {
      if (!this.isFor(Technique.TEMPORAL_CORE) || !this.canControl(player)) {
         return false;
      }
      if (this.getCasterUUID() == null) {
         this.setCaster(player);
      }

      this.fieldRadius = Math.max(3, Math.min(16, radius));
      this.fieldDurationSeconds = Math.max(1, Math.min(60, seconds));
      this.fieldMultiplier = Math.max(1, Math.min(4, multiplier));
      if (!this.getSwitchOn()) {
         super.toggle(player);
      }
      if (!this.getSwitchOn()) {
         return false;
      }

      this.fieldExpiresAt = this.level.getGameTime() + (long)this.fieldDurationSeconds * 20L;
      this.setChanged();
      this.sendUpdates();
      player.displayClientMessage(
         net.minecraft.network.chat.Component.translatable(
            "mahoudim.msg.core_field_started", this.fieldDurationSeconds, this.fieldRadius, this.fieldMultiplier
         ), true
      );
      return true;
   }

   @Override
   public void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putBoolean(SCROLL_CLAIMED, this.scrollClaimed);
      tag.putInt(FIELD_RADIUS, this.fieldRadius);
      tag.putInt(FIELD_MULTIPLIER, this.fieldMultiplier);
      tag.putInt(FIELD_DURATION, this.fieldDurationSeconds);
      tag.putLong(FIELD_EXPIRES, this.fieldExpiresAt);
   }

   @Override
   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.scrollClaimed = tag.getBoolean(SCROLL_CLAIMED);
      this.fieldRadius = Math.max(3, Math.min(16, tag.getInt(FIELD_RADIUS) == 0 ? 10 : tag.getInt(FIELD_RADIUS)));
      this.fieldMultiplier = Math.max(1, Math.min(4, tag.getInt(FIELD_MULTIPLIER) == 0 ? 1 : tag.getInt(FIELD_MULTIPLIER)));
      this.fieldDurationSeconds = Math.max(1, Math.min(60, tag.getInt(FIELD_DURATION) == 0 ? 20 : tag.getInt(FIELD_DURATION)));
      this.fieldExpiresAt = tag.getLong(FIELD_EXPIRES);
   }

   private Technique technique() {
      return this.getBlockState().getBlock() instanceof TechniqueCircleBlock block ? block.technique : null;
   }
}

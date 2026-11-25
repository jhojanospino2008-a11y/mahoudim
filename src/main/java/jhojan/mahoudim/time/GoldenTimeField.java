package jhojan.mahoudim.time;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenTimeField extends Entity {
    private static final EntityDataAccessor<Integer> SPEED = SynchedEntityData.defineId(GoldenTimeField.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING = SynchedEntityData.defineId(GoldenTimeField.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOTAL = SynchedEntityData.defineId(GoldenTimeField.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(GoldenTimeField.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(GoldenTimeField.class, EntityDataSerializers.INT);
    private BlockPos origin;
    private Block targetBlock;

    public GoldenTimeField(EntityType<? extends GoldenTimeField> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public GoldenTimeField(Level level, BlockPos pos, int duration, int radius, int mode, Block targetBlock) {
        this(RegAccess.entityType(), level);
        origin = pos.immutable();
        this.targetBlock = targetBlock;
        setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        setRemaining(duration);
        setTotal(duration < 0 ? 1 : duration);
        setRadius(radius);
        setMode(mode);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (origin == null || !level().getBlockState(origin).is(this.targetBlock)) {
            discard();
            return;
        }
        ServerLevel server = (ServerLevel) level();
        accelerate(server);
        if (getRemaining() >= 0) {
            setRemaining(getRemaining() - 1);
            if (getRemaining() <= 0) discard();
        }
    }

    private void accelerate(ServerLevel level) {
        int radius = getMode() == 0 ? 0 : getRadius();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (getMode() == 1 && targetBlock != null && !state.is(targetBlock)) continue;
                    tickBlock(level, pos.immutable(), state);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void tickBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        int extraTicks = Math.min(64, 1 << Math.min(6, getSpeed()));
        if (blockEntity != null) {
            BlockEntityTicker ticker = state.getTicker(level, blockEntity.getType());
            if (ticker != null) {
                for (int i = 0; i < extraTicks; i++) ticker.tick(level, pos, state, blockEntity);
            }
        } else if (state.isRandomlyTicking()) {
            for (int i = 0; i < extraTicks; i++) state.randomTick(level, pos, level.random);
        }
    }

    public void increaseSpeed() { setSpeed(Math.min(6, getSpeed() + 1)); }
    public int getSpeed() { return entityData.get(SPEED); }
    public void setSpeed(int value) { entityData.set(SPEED, Math.max(1, Math.min(6, value))); }
    public int getRemaining() { return entityData.get(REMAINING); }
    public void setRemaining(int value) { entityData.set(REMAINING, value); }
    public int getTotal() { return entityData.get(TOTAL); }
    public void setTotal(int value) { entityData.set(TOTAL, value); }
    public int getRadius() { return entityData.get(RADIUS); }
    public void setRadius(int value) { entityData.set(RADIUS, Math.max(1, Math.min(10, value))); }
    public int getMode() { return entityData.get(MODE); }
    public void setMode(int value) { entityData.set(MODE, Math.max(0, Math.min(2, value))); }
    public BlockPos getOrigin() { return origin; }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED, 1).define(REMAINING, 600).define(TOTAL, 600).define(RADIUS, 1).define(MODE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        origin = NbtUtils.readBlockPos(tag, "origin").orElse(null);
        setSpeed(tag.getInt("speed"));
        setRemaining(tag.getInt("remaining"));
        setTotal(tag.getInt("total"));
        setRadius(tag.getInt("radius"));
        setMode(tag.getInt("mode"));
        if (origin != null) targetBlock = level().getBlockState(origin).getBlock();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (origin != null) tag.put("origin", NbtUtils.writeBlockPos(origin));
        tag.putInt("speed", getSpeed());
        tag.putInt("remaining", getRemaining());
        tag.putInt("total", getTotal());
        tag.putInt("radius", getRadius());
        tag.putInt("mode", getMode());
    }

    private static final class RegAccess {
        private static EntityType<GoldenTimeField> entityType() { return jhojan.mahoudim.Reg.GOLDEN_TIME_FIELD.get(); }
    }
}

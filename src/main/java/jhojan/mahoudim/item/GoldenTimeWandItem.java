package jhojan.mahoudim.item;

import java.util.List;
import jhojan.mahoudim.time.GoldenTimeField;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GoldenTimeWandItem extends Item {
    public static final String DURATION = "GoldenTimeDuration";
    public static final String RADIUS = "GoldenTimeRadius";
    public static final String MODE = "GoldenTimeMode";
    public static final int INFINITE = -1;
    private static final int[] DURATIONS = {600, 1200, 2400, 6000, 12000, INFINITE};

    public GoldenTimeWandItem() { super(new Properties().stacksTo(1)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide) {
            AABB search = new AABB(hit.getBlockPos()).inflate(0.01D);
            GoldenTimeField existing = level.getEntitiesOfClass(GoldenTimeField.class, search, field -> hit.getBlockPos().equals(field.getOrigin()))
                .stream().findFirst().orElse(null);
            if (existing != null) {
                existing.increaseSpeed();
                existing.setRemaining(getDuration(stack));
                existing.setTotal(getDuration(stack) < 0 ? 1 : getDuration(stack));
                existing.setRadius(getRadius(stack));
                existing.setMode(getMode(stack));
                player.displayClientMessage(Component.translatable("mahoudim.golden_time_wand.accelerated", existing.getSpeed()), true);
            } else {
                var state = level.getBlockState(hit.getBlockPos());
                if (state.isAir()) return InteractionResultHolder.fail(stack);
                GoldenTimeField field = new GoldenTimeField(level, hit.getBlockPos(), getDuration(stack), getRadius(stack), getMode(stack), state.getBlock());
                level.addFreshEntity(field);
                player.displayClientMessage(Component.translatable("mahoudim.golden_time_wand.started"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static CompoundTag copyTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void putInt(ItemStack stack, String key, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(key, value));
    }

    public static void cycleDuration(ItemStack stack) {
        int current = getDuration(stack);
        int index = 0;
        for (int i = 0; i < DURATIONS.length; i++) if (DURATIONS[i] == current) index = i;
        putInt(stack, DURATION, DURATIONS[(index + 1) % DURATIONS.length]);
    }

    public static void cycleTarget(ItemStack stack) {
        int mode = getMode(stack);
        int radius = getRadius(stack);
        if (mode == 0) { putInt(stack, MODE, 1); putInt(stack, RADIUS, 1); }
        else if (mode == 1 && radius < 10) putInt(stack, RADIUS, radius + 1);
        else if (mode == 1) putInt(stack, MODE, 2);
        else if (mode == 2 && radius < 10) putInt(stack, RADIUS, radius + 1);
        else { putInt(stack, MODE, 0); putInt(stack, RADIUS, 1); }
    }

    public static int getDuration(ItemStack stack) {
        int value = copyTag(stack).getInt(DURATION);
        return value == 0 ? DURATIONS[0] : value;
    }
    public static int getRadius(ItemStack stack) {
        int value = copyTag(stack).getInt(RADIUS);
        return Math.max(1, Math.min(10, value == 0 ? 1 : value));
    }
    public static int getMode(ItemStack stack) { return Math.max(0, Math.min(2, copyTag(stack).getInt(MODE))); }

    public static String durationText(ItemStack stack) {
        int duration = getDuration(stack);
        return duration < 0 ? "∞" : (duration / 20) + " s";
    }

    public static String targetText(ItemStack stack) {
        return switch (getMode(stack)) {
            case 1 -> "mismo bloque, radio " + getRadius(stack);
            case 2 -> "todos los bloques, radio " + getRadius(stack);
            default -> "1 bloque";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("mahoudim.golden_time_wand.duration", durationText(stack)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("mahoudim.golden_time_wand.target", targetText(stack)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("mahoudim.golden_time_wand.controls").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("mahoudim.golden_time_wand.free").withStyle(ChatFormatting.GREEN));
    }
}

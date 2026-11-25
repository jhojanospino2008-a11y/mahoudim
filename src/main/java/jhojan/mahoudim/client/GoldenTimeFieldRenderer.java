package jhojan.mahoudim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import jhojan.mahoudim.time.GoldenTimeField;

public class GoldenTimeFieldRenderer extends EntityRenderer<GoldenTimeField> {
    public GoldenTimeFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(GoldenTimeField field, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        BlockPos origin = field.blockPosition();
        pose.pushPose();
        pose.translate(-origin.getX(), -origin.getY(), -origin.getZ());
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(pose, lines, new AABB(origin).inflate(0.012D), 1.0F, 0.78F, 0.08F, 1.0F);
        pose.popPose();

        pose.pushPose();
        pose.translate(0.5D, 1.35D, 0.5D);
        String rate = "x" + (1 << Math.max(0, field.getSpeed()));
        String time = field.getRemaining() < 0 ? "∞" : String.format("%.0fs", field.getRemaining() / 20.0F);
        String range = "r" + field.getRadius();
        drawLabel(pose, buffer, rate + "  " + time, 0.0F, 0.0F, ChatFormatting.GOLD.getColor());
        drawLabel(pose, buffer, range, 0.0F, 0.14F, ChatFormatting.YELLOW.getColor());
        pose.popPose();
    }

    private void drawLabel(PoseStack pose, MultiBufferSource buffer, String text, float x, float y, int color) {
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(-0.018F, -0.018F, 0.018F);
        float width = getFont().width(text);
        getFont().drawInBatch(Component.literal(text), x - width / 2.0F, y, color, false, pose.last().pose(), buffer, DisplayMode.NORMAL, 0x55000000, 15728880);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenTimeField field) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}

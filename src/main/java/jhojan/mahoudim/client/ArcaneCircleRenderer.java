package jhojan.mahoudim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.Util;

/** Lightweight, animated Doctor-Strange-inspired runic rings for the Second Sorcery Code. */
public final class ArcaneCircleRenderer {
   private static final int SEGMENTS = 96;

   private ArcaneCircleRenderer() {
   }

   public static void render(PoseStack poseStack, MultiBufferSource buffers) {
      VertexConsumer lines = buffers.getBuffer(RenderType.lines());
      PoseStack.Pose pose = poseStack.last();
      float time = (Util.getMillis() % 60000L) / 1000.0F;
      float spinA = time * 0.42F;
      float spinB = -time * 0.31F;
      float spinC = time * 0.55F;

      // Face-on circle, broken into rotating arc segments with rune-like radial ticks.
      ring(lines, pose, 0.72F, 0.045F, 0.0F, 0.10F, 1.0F, 0.53F, 0.18F, spinA, 6, 3, 0.0F, 0.0F, 1.0F);
      ticks(lines, pose, 0.72F, 0.045F, 0.0F, 0.10F, 1.0F, 0.72F, 0.29F, spinA, 24, 0.035F, 0.075F, 0.0F, 0.0F, 1.0F);

      // Horizontal orbit, complementing the purple/cyan crystal colors in the Code model.
      ringBasis(lines, pose, 0.91F, 0.0F, 0.025F, 0.0F, 0.72F, 0.22F, 1.0F, spinB, 8, 2,
         1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      ticksBasis(lines, pose, 0.91F, 0.0F, 0.025F, 0.0F, 0.82F, 0.48F, 1.0F, spinB, 32, 0.025F, 0.065F,
         1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);

      // A tilted orbital circle gives the held item a layered, dimensional silhouette.
      float ux = (float)Math.cos(spinC);
      float uz = (float)Math.sin(spinC);
      float tilt = 0.42F;
      float vx = -uz * tilt;
      float vy = 0.91F;
      float vz = ux * tilt;
      ringBasis(lines, pose, 0.82F, 0.0F, 0.0F, 0.0F, 0.20F, 0.82F, 1.0F, spinC, 10, 2, ux, 0.0F, uz, vx, vy, vz);
      ticksBasis(lines, pose, 0.82F, 0.0F, 0.0F, 0.0F, 0.48F, 0.96F, 1.0F, spinC, 20, 0.03F, 0.08F, ux, 0.0F, uz, vx, vy, vz);
   }

   private static void ring(VertexConsumer out, PoseStack.Pose pose, float radius, float x, float y, float z,
      float red, float green, float blue, float phase, int gapPeriod, int gapLength, float nx, float ny, float nz) {
      for (int i = 0; i < SEGMENTS; i++) {
         if (gapPeriod > 0 && i % gapPeriod < gapLength) {
            continue;
         }
         float a0 = (float)(Math.PI * 2.0 * i / SEGMENTS) + phase;
         float a1 = (float)(Math.PI * 2.0 * (i + 1) / SEGMENTS) + phase;
         line(out, pose, x + radius * (float)Math.cos(a0), y + radius * (float)Math.sin(a0), z,
            x + radius * (float)Math.cos(a1), y + radius * (float)Math.sin(a1), z, red, green, blue, 0.82F, nx, ny, nz);
      }
   }

   private static void ticks(VertexConsumer out, PoseStack.Pose pose, float radius, float x, float y, float z,
      float red, float green, float blue, float phase, int count, float shortLength, float longLength,
      float nx, float ny, float nz) {
      for (int i = 0; i < count; i++) {
         float a = (float)(Math.PI * 2.0 * i / count) + phase;
         float length = i % 4 == 0 ? longLength : shortLength;
         float c = (float)Math.cos(a);
         float s = (float)Math.sin(a);
         line(out, pose, x + (radius - length * 0.5F) * c, y + (radius - length * 0.5F) * s, z,
            x + (radius + length * 0.5F) * c, y + (radius + length * 0.5F) * s, z, red, green, blue, 0.95F, nx, ny, nz);
      }
   }

   private static void ringBasis(VertexConsumer out, PoseStack.Pose pose, float radius, float x, float y, float z,
      float red, float green, float blue, float phase, int gapPeriod, int gapLength,
      float ux, float uy, float uz, float vx, float vy, float vz) {
      for (int i = 0; i < SEGMENTS; i++) {
         if (gapPeriod > 0 && i % gapPeriod < gapLength) {
            continue;
         }
         float a0 = (float)(Math.PI * 2.0 * i / SEGMENTS) + phase;
         float a1 = (float)(Math.PI * 2.0 * (i + 1) / SEGMENTS) + phase;
         float c0 = radius * (float)Math.cos(a0);
         float s0 = radius * (float)Math.sin(a0);
         float c1 = radius * (float)Math.cos(a1);
         float s1 = radius * (float)Math.sin(a1);
         line(out, pose, x + c0 * ux + s0 * vx, y + c0 * uy + s0 * vy, z + c0 * uz + s0 * vz,
            x + c1 * ux + s1 * vx, y + c1 * uy + s1 * vy, z + c1 * uz + s1 * vz,
            red, green, blue, 0.88F, uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);
      }
   }

   private static void ticksBasis(VertexConsumer out, PoseStack.Pose pose, float radius, float x, float y, float z,
      float red, float green, float blue, float phase, int count, float shortLength, float longLength,
      float ux, float uy, float uz, float vx, float vy, float vz) {
      for (int i = 0; i < count; i++) {
         float a = (float)(Math.PI * 2.0 * i / count) + phase;
         float length = i % 4 == 0 ? longLength : shortLength;
         float c = (float)Math.cos(a);
         float s = (float)Math.sin(a);
         float r0 = radius - length * 0.5F;
         float r1 = radius + length * 0.5F;
         line(out, pose,
            x + r0 * c * ux + r0 * s * vx, y + r0 * c * uy + r0 * s * vy, z + r0 * c * uz + r0 * s * vz,
            x + r1 * c * ux + r1 * s * vx, y + r1 * c * uy + r1 * s * vy, z + r1 * c * uz + r1 * s * vz,
            red, green, blue, 0.98F, uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);
      }
   }

   private static void line(VertexConsumer out, PoseStack.Pose pose, float x0, float y0, float z0,
      float x1, float y1, float z1, float red, float green, float blue, float alpha,
      float nx, float ny, float nz) {
      out.addVertex(pose, x0, y0, z0).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz);
      out.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz);
   }
}

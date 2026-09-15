package io.github.mortuusars.exposure.client.render.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.render.RenderQuadSink;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class TextureRenderer {
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, Color color) {
        render(poseStack, bufferSource, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, RenderQuadSink.of(bufferSource), texture, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        render(poseStack, RenderQuadSink.of(bufferSource), texture, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        render(poseStack, RenderQuadSink.of(bufferSource), texture, minX, minY, maxX, maxY,
                minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, RenderQuadSink sink, Identifier texture,
                              int packedLight, Color color) {
        render(poseStack, sink, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void render(PoseStack poseStack, RenderQuadSink sink, Identifier texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, sink, texture, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, RenderQuadSink sink, Identifier texture,
                              float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        render(poseStack, sink, texture, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, RenderQuadSink sink, Identifier texture,
                              float minX, float minY, float maxX, float maxY,
                              float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        sink.draw(RenderTypes.text(texture), poseStack, (matrix, vertexConsumer) -> {
            vertexConsumer.addVertex(matrix, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
        });
    }
}

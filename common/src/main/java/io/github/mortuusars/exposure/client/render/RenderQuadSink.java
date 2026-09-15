package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;

/**
 * Abstraction over the two ways the game submits quads in 1.21.11:
 * - GUI screens still use {@link MultiBufferSource}.
 * - Entity/level rendering uses {@link SubmitNodeCollector} (via {@link SubmitNodeCollector#submitCustomGeometry}).
 */
public interface RenderQuadSink {
    void draw(RenderType renderType, PoseStack poseStack, QuadDrawer draw);

    interface QuadDrawer {
        void draw(Matrix4f matrix, VertexConsumer vertexConsumer);
    }

    static RenderQuadSink of(MultiBufferSource bufferSource) {
        return (renderType, poseStack, draw) -> draw.draw(poseStack.last().pose(), bufferSource.getBuffer(renderType));
    }

    static RenderQuadSink of(SubmitNodeCollector collector) {
        return (renderType, poseStack, draw) ->
                collector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> draw.draw(pose.pose(), vertexConsumer));
    }
}

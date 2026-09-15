package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemStack;

public class ItemFramePhotographRenderer {
    public static void render(ItemStack stack, PoseStack poseStack, RenderQuadSink sink,
                              int packedLight, PhotographItem item, int rotation, boolean isGlowFrame) {
        if (isGlowFrame)
            packedLight = LightTexture.FULL_BRIGHT;

        poseStack.pushPose();

        // Snap to 90 degrees like a map.
        poseStack.mulPose(Axis.ZP.rotationDegrees(45 * rotation));

        float pixelSize = 0.0625f;
        float scale = 1f - pixelSize * 6; // 3px from each side

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, 0.045);

        ExposureClient.photographRenderer().renderPhotograph(poseStack, sink, item, stack,
                false, false, packedLight, 255, 255, 255, 255);

        poseStack.popPose();
    }
}

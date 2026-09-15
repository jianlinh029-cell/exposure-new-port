package io.github.mortuusars.exposure.client.render.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.RenderQuadSink;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.texture.TextureRenderer;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PhotographRenderer {
    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                          MultiBufferSource bufferSource, int packedLight) {
        return render(itemStack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, 255, 255, 255, 255);
    }

    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                          MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a) {
        return render(itemStack, renderPaper, renderBackside, poseStack, RenderQuadSink.of(bufferSource), packedLight, r, g, b, a);
    }

    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                          RenderQuadSink sink, int packedLight) {
        return render(itemStack, renderPaper, renderBackside, poseStack, sink, packedLight, 255, 255, 255, 255);
    }

    public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack,
                          RenderQuadSink sink, int packedLight, int r, int g, int b, int a) {
        if (itemStack.getItem() instanceof PhotographItem photographItem)
            return renderPhotograph(poseStack, sink, photographItem, itemStack, renderPaper, renderBackside, packedLight, r, g, b, a);
        else if (itemStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem)
            return renderStackedPhotographs(stackedPhotographsItem, itemStack, poseStack, sink, packedLight, r, g, b, a);
        return false;
    }

    public boolean renderPhotograph(PoseStack poseStack, MultiBufferSource bufferSource,
                                    PhotographItem photographItem, ItemStack photographStack,
                                    boolean renderPaper, boolean renderBackside, int packedLight, int r, int g, int b, int a) {
        return renderPhotograph(poseStack, RenderQuadSink.of(bufferSource), photographItem, photographStack,
                renderPaper, renderBackside, packedLight, r, g, b, a);
    }

    public boolean renderPhotograph(PoseStack poseStack, RenderQuadSink sink,
                                    PhotographItem photographItem, ItemStack photographStack,
                                    boolean renderPaper, boolean renderBackside, int packedLight, int r, int g, int b, int a) {
        PhotographStyle style = PhotographStyle.of(photographStack);

        Frame frame = photographItem.getFrame(photographStack);

        RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));

        int paperRotation = frame.identifier().hashCode() % 4 * 90;

        if (renderPaper && style.paperTexture() != ExposureClient.Textures.EMPTY) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            TextureRenderer.render(poseStack, sink, style.paperTexture(), packedLight, r, g, b, a);

            poseStack.popPose();

            if (renderBackside) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.5, 0, -0.5);

                poseStack.translate(0.5f, 0.5f, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
                poseStack.translate(-0.5f, -0.5f, 0);

                TextureRenderer.render(poseStack, sink, style.paperTexture(),
                        packedLight, (int) (r * 0.85f), (int) (g * 0.85f), (int) (b * 0.85f), a);

                poseStack.popPose();
            }
        }

        if (renderPaper) {
            poseStack.pushPose();
            float offset = 0.0625f;
            poseStack.translate(offset, offset, 0.001);
            poseStack.scale(0.875f, 0.875f, 0.875f);
            ExposureClient.imageRenderer().render(image, poseStack, sink, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
            poseStack.popPose();
        } else {
            ExposureClient.imageRenderer().render(image, poseStack, sink, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
        }

        if (renderPaper && style.hasOverlayTexture()) {
            poseStack.pushPose();

            poseStack.translate(0.5f, 0.5f, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(paperRotation));
            poseStack.translate(-0.5f, -0.5f, 0);

            poseStack.translate(0, 0, 0.002);
            TextureRenderer.render(poseStack, sink, style.overlayTexture(), packedLight, r, g, b, a);
            poseStack.popPose();
        }

        return !image.isEmpty();
    }

    public boolean renderStackedPhotographs(StackedPhotographsItem stackedPhotographsItem, ItemStack stack,
                                            PoseStack poseStack, MultiBufferSource bufferSource,
                                            int packedLight, int r, int g, int b, int a) {
        return renderStackedPhotographs(stackedPhotographsItem, stack, poseStack, RenderQuadSink.of(bufferSource),
                packedLight, r, g, b, a);
    }

    public boolean renderStackedPhotographs(StackedPhotographsItem stackedPhotographsItem, ItemStack stack,
                                            PoseStack poseStack, RenderQuadSink sink,
                                            int packedLight, int r, int g, int b, int a) {
        List<ItemAndStack<PhotographItem>> photographs = stackedPhotographsItem.getPhotographs(stack).photographsItemAndStacks();
        return renderStackedPhotographs(photographs, poseStack, sink, packedLight, r, g, b, a);
    }

    public boolean renderStackedPhotographs(List<ItemAndStack<PhotographItem>> photographs,
                                            PoseStack poseStack, MultiBufferSource bufferSource,
                                            int packedLight, int r, int g, int b, int a) {
        return renderStackedPhotographs(photographs, poseStack, RenderQuadSink.of(bufferSource), packedLight, r, g, b, a);
    }

    public boolean renderStackedPhotographs(List<ItemAndStack<PhotographItem>> photographs,
                                            PoseStack poseStack, RenderQuadSink sink,
                                            int packedLight, int r, int g, int b, int a) {
        if (photographs.isEmpty()) return false;

        boolean photographRendered = false;

        for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 < i)
                continue;

            ItemAndStack<PhotographItem> photograph = photographs.get(i);

            // Top photograph:
            if (i == 0) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.002);
                photographRendered = renderPhotograph(poseStack, sink, photograph.getItem(), photograph.getItemStack(),
                        true, false, packedLight, r, g, b, a);
                poseStack.popPose();
                break;
            }

            // Photographs below (only paper)
            float posOffset = getStackedPhotographOffset() * i;

            poseStack.pushPose();
            poseStack.translate(posOffset, posOffset, 0.002 - i / 1000f);

            poseStack.translate(0.5f, 0.5f, 0);

            Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
            int rotation = frame.identifier().hashCode() % 4 * 90;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

            poseStack.translate(-0.5f, -0.5f, 0);

            float brightness = 1f - (getStackedBrightnessStep() * i);

            PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());

            TextureRenderer.render(poseStack, sink, photographStyle.paperTexture(),
                    packedLight, (int)(r * brightness), (int)(g * brightness), (int)(b * brightness), a);

            poseStack.popPose();
        }

        return photographRendered;
    }

    public float getStackedBrightnessStep() {
        return 0.2f;
    }

    public float getStackedPhotographOffset() {
        // 2 px / Texture size (64px) = 0.03125
        return 0.03125f;
    }

    // =============================================================================================
    // Unified 1.21.11 GUI rendering path.
    // GUI photographs MUST go through GuiGraphics + RenderPipelines.GUI_TEXTURED + the cached
    // DynamicTexture (ImageRenderer). The old world MultiBufferSource path cannot be batched by the
    // deferred GUI render state and was producing grey/blank/partially sampled photos on screens.
    // =============================================================================================

    /** Resolve the style-processed RenderableImage for a photograph ItemStack. */
    public RenderableImage processedImage(ItemStack photographStack) {
        if (photographStack.getItem() instanceof PhotographItem photographItem) {
            PhotographStyle style = PhotographStyle.of(photographStack);
            Frame frame = photographItem.getFrame(photographStack);
            return style.process(ExposureClient.renderedExposures().getOrCreate(frame));
        }
        return RenderableImage.EMPTY;
    }

    /** Draw just the photograph image (no paper), the whole frame scaled to the target rect. */
    public boolean renderImageInGui(GuiGraphics guiGraphics, RenderableImage image,
                                   int x, int y, int width, int height, int r, int g, int b, int a) {
        if (image == null || image.isEmpty()) return false;
        ExposureClient.imageRenderer().render(image, guiGraphics, x, y, width, height, Color.argb(a, r, g, b));
        return true;
    }

    public boolean renderImageInGui(GuiGraphics guiGraphics, ItemStack photographStack,
                                   int x, int y, int width, int height, int r, int g, int b, int a) {
        return renderImageInGui(guiGraphics, processedImage(photographStack), x, y, width, height, r, g, b, a);
    }

    /**
     * Draw a complete photograph (paper + image + overlay) in a GUI, mirroring the world-model
     * layout (image inset by 1/16 = the old 0.0625 translate / 0.875 scale). Used by surfaces that
     * do not draw their own paper, e.g. the item tooltip preview.
     */
    public boolean renderPhotographInGui(GuiGraphics guiGraphics, ItemStack photographStack,
                                         int x, int y, int width, int height, int r, int g, int b, int a) {
        if (!(photographStack.getItem() instanceof PhotographItem)) return false;

        PhotographStyle style = PhotographStyle.of(photographStack);

        if (style.paperTexture() != ExposureClient.Textures.EMPTY) {
            blitWhole(guiGraphics, style.paperTexture(), x, y, width, height, r, g, b, a);
        }

        int inset = Math.round(width * 0.0625f);
        boolean rendered = renderImageInGui(guiGraphics, photographStack,
                x + inset, y + inset, width - inset * 2, height - inset * 2, r, g, b, a);

        if (style.hasOverlayTexture()) {
            blitWhole(guiGraphics, style.overlayTexture(), x, y, width, height, r, g, b, a);
        }

        return rendered;
    }

    /** Stacked photographs in a GUI: top one fully rendered, lower stacks paper-only with an offset. */
    public boolean renderStackedInGui(GuiGraphics guiGraphics, List<ItemAndStack<PhotographItem>> photographs,
                                      int x, int y, int width, int height, int r, int g, int b, int a) {
        if (photographs.isEmpty()) return false;

        boolean photographRendered = false;
        float step = getStackedPhotographOffset();

        for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 < i) continue;

            ItemAndStack<PhotographItem> photograph = photographs.get(i);

            if (i == 0) {
                photographRendered = renderPhotographInGui(guiGraphics, photograph.getItemStack(),
                        x, y, width, height, r, g, b, a);
                break;
            }

            int offset = Math.round(step * width) * i;
            float brightness = 1f - (getStackedBrightnessStep() * i);
            int rr = clampByte(r * brightness);
            int gg = clampByte(g * brightness);
            int bb = clampByte(b * brightness);

            PhotographStyle style = PhotographStyle.of(photograph.getItemStack());
            if (style.paperTexture() != ExposureClient.Textures.EMPTY) {
                blitWhole(guiGraphics, style.paperTexture(), x + offset, y + offset, width, height, rr, gg, bb, a);
            }
        }

        return photographRendered;
    }

    private static void blitWhole(GuiGraphics guiGraphics, net.minecraft.resources.Identifier texture,
                                  int x, int y, int width, int height, int r, int g, int b, int a) {
        int color = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        // region == drawn size == supplied size => normalized UV 0..1 (whole texture), scaled to rect.
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, width, height, width, height, color);
    }

    private static int clampByte(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }
}

package io.github.mortuusars.exposure.client.render.image;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.RenderQuadSink;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Credits to <a href="https://github.com/Jalvaviel/MapMipMapMod">MapMipMapMod by Jalvaviel</a> for example of mipmap implementation for dynamic images.
 * And to <a href="https://github.com/bravely-beep">bravely-beep</a> for pointing me to it.
 */
public class RenderedImageInstance implements AutoCloseable {
    protected final Identifier textureLocation;
    protected RenderableImage image;
    protected DynamicTexture texture;
    protected final RenderType renderType;
    protected boolean requiresUpload = true;

    RenderedImageInstance(RenderableImage image) {
        this.image = image;
        this.textureLocation = image.getIdentifier().toIdentifier();
        this.texture = createTexture();
        Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);

        renderType = RenderTypes.text(textureLocation);

        forceUpload();
    }

    private DynamicTexture createTexture() {
        return new DynamicTexture(() -> "exposure_dynamic_image_" + image.getIdentifier().toIdentifier().getPath(),
                image.width(), image.height(), true);
    }

    /**
     * Re-bind the dynamic texture if it has been released externally (texture manager reset, etc.).
     * A DynamicTexture is not a ReloadableTexture, so normally it survives resource reloads; this
     * is only a safety net to avoid drawing a missing/white texture after an unexpected release.
     */
    private void ensureTextureBound() {
        AbstractTexture registered = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);
        if (registered != this.texture) {
            try {
                this.texture.close();
            } catch (Exception ignored) {
            }
            this.texture = createTexture();
            Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);
            this.requiresUpload = true;
        }
    }

    public void replaceData(RenderableImage image) {
        boolean sizeChanged = image.width() != this.image.width() || image.height() != this.image.height();

        this.image = image;

        // DynamicTexture is allocated at a fixed resolution, so a size change requires rebuilding
        // the GPU texture. When only the pixels change (same identifier, same size) we keep the
        // texture and simply re-upload below.
        if (sizeChanged) {
            Minecraft.getInstance().getTextureManager().release(textureLocation);
            try {
                this.texture.close();
            } catch (Exception ignored) {
            }
            this.texture = createTexture();
            Minecraft.getInstance().getTextureManager().register(textureLocation, this.texture);
        }

        // Always request an upload: a RenderableImage may carry brand-new pixel data while keeping
        // the same identifier, so identifier equality must never suppress the texture upload.
        forceUpload();
    }

    public void forceUpload() {
        this.requiresUpload = true;
    }

    protected void updateTexture() {
        if (texture.getPixels() == null) return;

        int width = this.image.width();
        int height = this.image.height();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ARGB = this.image.getPixelARGB(x, y);
                // NativeImage stores pixels in ABGR order.
                int ABGR = (ARGB & 0xFF00FF00) | ((ARGB & 0x000000FF) << 16) | ((ARGB & 0x00FF0000) >> 16);
                this.texture.getPixels().setPixelABGR(x, y, ABGR);
            }
        }

        this.texture.upload();
    }

    public void draw(PoseStack poseStack, RenderQuadSink sink, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        sink.draw(this.renderType, poseStack, (matrix, vertexConsumer) -> {
            vertexConsumer.addVertex(matrix, minX, maxY, 0).setColor(r, g, b, a).setUv(minU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, maxX, maxY, 0).setColor(r, g, b, a).setUv(maxU, maxV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, maxX, minY, 0).setColor(r, g, b, a).setUv(maxU, minV).setLight(packedLight);
            vertexConsumer.addVertex(matrix, minX, minY, 0).setColor(r, g, b, a).setUv(minU, minV).setLight(packedLight);
        });
    }

    public void draw(PoseStack poseStack, MultiBufferSource bufferSource, float minX, float minY, float maxX, float maxY,
                     float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        draw(poseStack, RenderQuadSink.of(bufferSource), minX, minY, maxX, maxY, minU, minV, maxU, maxV, packedLight, r, g, b, a);
    }

    /**
     * 1.21.11 GUI rendering path. GuiGraphics is deferred, so GUI photographs must be submitted
     * through the GUI render state (GUI_TEXTURED) instead of a world MultiBufferSource.
     * <p>
     * The whole source image is sampled (normalized UV 0..1) and scaled to {@code width x height}.
     * The 13-arg blit overload is used on purpose: the 11-arg shortcut forces the sampled region to
     * equal the on-screen size, which would yield u2 = screenWidth / textureWidth and only show the
     * top-left corner of the photograph instead of the whole frame.
     */
    public void draw(GuiGraphics guiGraphics, int x, int y, int width, int height, int r, int g, int b, int a) {
        ensureTextureBound();
        if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
        }

        int sourceWidth = this.image.width();
        int sourceHeight = this.image.height();
        int color = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        // pipeline, id, x, y, u, v, drawW, drawH, regionW, regionH, texW, texH, color
        // region == full texture => u1=0, u2=regionW/texW=1 (and same for v): whole photo, scaled.
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.textureLocation,
                x, y, 0f, 0f, width, height, sourceWidth, sourceHeight, sourceWidth, sourceHeight, color);
    }

    public void close() {
        Minecraft.getInstance().getTextureManager().release(textureLocation);
        try {
            this.texture.close();
        } catch (Exception ignored) {
        }
    }
}

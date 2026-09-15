package io.github.mortuusars.exposure.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.util.Rect2f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

/**
 * 1.21.11: GuiGraphics.pose() returns a 2D Matrix3x2fStack and the old immediate-mode
 * (RenderSystem.setShader + Tesselator) rendering is gone. All drawing now goes through
 * GuiGraphics with RenderPipelines.
 */
public class GuiUtil {
    /** Snapshots the current 2D GUI matrix into a 3D PoseStack (for photo/texture rendering that requires one). */
    public static PoseStack copyPose(Matrix3x2fStack pose2d) {
        PoseStack pose = new PoseStack();
        pose.last().pose().mul(pose2d);
        return pose;
    }

    public static void blit(GuiGraphics guiGraphics, @Nullable Identifier texture, Rect2f rect,
                            int u, int v, int textureWidth, int textureHeight) {
        if (texture != null) {
            // 1.21.11: textured quads MUST use GUI_TEXTURED (POSITION_TEX_COLOR + Sampler0); the plain GUI
            // pipeline is POSITION_COLOR with no sampler and renders the UV-less quad as solid white.
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) rect.x, (int) rect.y, u, v,
                    (int) rect.width, (int) rect.height, textureWidth, textureHeight);
        }
    }

    public static void blit(GuiGraphics guiGraphics, Identifier texture, float x, float y, float width, float height,
                            int u, int v, int textureWidth, int textureHeight) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) x, (int) y, u, v,
                (int) width, (int) height, textureWidth, textureHeight);
    }

    /**
     * Draws a {@code regionWidth x regionHeight} source region (from a {@code texWidth x texHeight} texture)
     * at {@code (x,y)} scaled to {@code width x height}. Use when draw size differs from source size.
     */
    public static void blit(GuiGraphics guiGraphics, Identifier texture, int x, int y, int width, int height,
                            float u, float v, int regionWidth, int regionHeight, int texWidth, int texHeight) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v,
                width, height, regionWidth, regionHeight, texWidth, texHeight);
    }

    /**
     * Stretches the whole texture (normalized UV 0..1) over the given rect. Use when the source
     * texture size differs from the on-screen size (e.g. the 256x256 viewfinder filling the opening).
     */
    public static void blitFull(GuiGraphics guiGraphics, Identifier texture, Rect2f rect) {
        int w = (int) rect.width;
        int h = (int) rect.height;
        // 12-arg form: draw w*h, sample a w*h source region from a w*h "atlas" => normalized UV 0..1.
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, (int) rect.x, (int) rect.y, 0f, 0f,
                w, h, w, h, w, h);
    }

    // --

    public static void drawRect(GuiGraphics guiGraphics, Rect2f rect, int color) {
        drawRect(guiGraphics, rect.x, rect.y, rect.width, rect.height, color);
    }

    public static void drawRect(GuiGraphics guiGraphics, float x, float y, float width, float height, int color) {
        guiGraphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    // --

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int width, int color) {
        renderScrollingString(guiGraphics, font, text, x, y, x + width, y + font.lineHeight, color);
    }

    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color);
    }

    // Doesn't work in toast for some reason.
    public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color) {
        int fontWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int width = maxX - minX;
        if (fontWidth > width) {
            int remaining = fontWidth - width;
            double d = (double) Util.getMillis() / 400;
            double e = Math.max((double) remaining * 0.5, 3.0);
            double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, remaining);
            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX - (int) g, y, color, false);
            guiGraphics.disableScissor();
        } else {
            int l = Mth.clamp(centerX, minX + fontWidth / 2, maxX - fontWidth / 2);
            guiGraphics.drawCenteredString(font, text, l, y, color);
        }
    }
}

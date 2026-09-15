package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.Fov;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public class FocalLengthButton extends ImageButton {
    protected final int secondaryFontColor;
    protected final int mainFontColor;

    // [CameraZoomDebug] throttle HUD log (renderContents runs every frame).
    private static long lastZoomDebugLogMs = 0L;

    public FocalLengthButton(int x, int y, int width, int height, WidgetSprites sprites) {
        super(x, y, width, height, sprites, button -> {}, Component.empty());
        mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
        secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
    }

    @Override
    public void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick);

        double fov = getCurrentFov();
        double rawFocalLength = Fov.fovToFocalLength(fov);
        int focalLength = (int) Math.round(rawFocalLength);

        Font font = Minecraft.getInstance().font;
        MutableComponent text = Component.translatable("gui.exposure.camera_controls.focal_length", focalLength);
        int textWidth = font.width(text);
        int xPos = 17 + (29 - textWidth) / 2;

        guiGraphics.drawString(font, text, getX() + xPos, getY() + 8, secondaryFontColor, false);
        guiGraphics.drawString(font, text, getX() + xPos, getY() + 7, mainFontColor, false);

        // [CameraZoomDebug] zoom = stored component; focalLength = raw; displayFocalLength = rounded shown value.
        long now = System.currentTimeMillis();
        if (now - lastZoomDebugLogMs > 400L) {
            lastZoomDebugLogMs = now;
            Viewfinder viewfinder = CameraClient.viewfinder();
            float zoom = viewfinder != null ? viewfinder.zoom().getStoredZoom() : -1f;
            Exposure.LOGGER.info("[CameraZoomDebug] zoom={} fov={} focalLength={} displayFocalLength={}",
                    String.format("%.3f", zoom), String.format("%.3f", fov),
                    String.format("%.2f", rawFocalLength), focalLength);
        }
    }

    protected double getCurrentFov() {
        // 1.21.11: GameRenderer#getFov is no longer accessible here. The value actually driving the
        // zoomed viewfinder is ViewfinderZoom#getCurrentFov (FovModifier feeds the same value into
        // the renderer); reading options.fov() returned the fixed base FOV and froze the readout.
        Viewfinder viewfinder = CameraClient.viewfinder();
        if (viewfinder != null && viewfinder.isLookingThrough()) {
            return viewfinder.zoom().getCurrentFov();
        }
        return Minecrft.get().options.fov().get().doubleValue();
    }
}

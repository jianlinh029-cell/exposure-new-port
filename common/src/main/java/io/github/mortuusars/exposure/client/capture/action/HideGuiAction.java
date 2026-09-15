package io.github.mortuusars.exposure.client.capture.action;

import net.minecraft.client.Minecraft;

public class HideGuiAction implements CaptureAction {
    private boolean hideGuiBeforeCapture;

    @Override
    public void beforeCapture() {
        hideGuiBeforeCapture = Minecraft.getInstance().options.hideGui;
        Minecraft.getInstance().options.hideGui = true;
    }

    @Override
    public void afterCapture() {
        Minecraft.getInstance().options.hideGui = hideGuiBeforeCapture;
    }
}

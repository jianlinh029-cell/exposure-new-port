package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.render.GammaModifier;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.client.Minecraft;

public class ModifyGammaAction implements CaptureAction {
    protected final float offset;

    public ModifyGammaAction(float offset) {
        this.offset = offset;
    }

    public ModifyGammaAction(float brightnessStops, float gammaPerStop) {
        float currentGamma = Minecraft.getInstance().options.gamma().get().floatValue();
        float strength = (1f - currentGamma) * 0.65f + 0.35f;
        this.offset = (gammaPerStop * brightnessStops) * strength;
    }

    public ModifyGammaAction(ShutterSpeed shutterSpeed) {
        this(shutterSpeed.getStops(), 0.03f);
    }

    @Override
    public void beforeCapture() {
        GammaModifier.apply(offset);
    }

    @Override
    public void afterCapture() {
        GammaModifier.restore();
    }
}

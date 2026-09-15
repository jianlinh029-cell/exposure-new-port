package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.render.FovModifier;

public class SetFovAction implements CaptureAction {
    protected final double fov;

    public SetFovAction(double fov) {
        this.fov = fov;
    }

    @Override
    public void beforeCapture() {
        FovModifier.setOverride(fov);
    }

    @Override
    public void afterCapture() {
        FovModifier.cancelOverride();
    }
}

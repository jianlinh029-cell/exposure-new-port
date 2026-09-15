package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.util.Shader;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class SetFilterAction implements CaptureAction {
    protected Optional<Identifier> filter;

    public SetFilterAction(Optional<Identifier> filter) {
        this.filter = filter;
    }

    @Override
    public void beforeCapture() {
        Shader.setSuppressViewfinder(true);
        // Never carry a previous capture's filter into the next photo.
        CaptureShader.remove();
        filter.ifPresent(CaptureShader::apply);
    }

    @Override
    public void afterCapture() {
        Shader.setSuppressViewfinder(false);
        CaptureShader.remove();
    }
}

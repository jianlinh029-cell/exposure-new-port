package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.util.Minecrft;

/**
 * 1.21.11: GameRenderer#setPostEffect is private and PostChain is managed by ShaderManager,
 * so setting/restoring a temporary post effect from a capture action is no longer supported.
 * Kept as a no-op for API compatibility.
 */
public class SetPostEffectAction implements CaptureAction {
    private boolean wasActive;

    public SetPostEffectAction(net.minecraft.resources.Identifier effect) {
    }

    @Override
    public void beforeCapture() {
        wasActive = Minecrft.get().gameRenderer.effectActive;
    }

    @Override
    public void afterCapture() {
        Minecrft.get().gameRenderer.effectActive = wasActive;
    }
}

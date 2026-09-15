package io.github.mortuusars.exposure.client.capture;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CaptureShader {
    @Nullable
    private static Identifier shader = null;

    public static boolean hasShader() {
        return shader != null;
    }

    public static void apply(Identifier shaderLocation) {
        if (shader != null && shader.equals(shaderLocation)) {
            return;
        }

        shader = shaderLocation;
        Exposure.LOGGER.info("[FilterDebug] captureFilter applied captureShader={}", shaderLocation);
    }



    public static void process() {
        if (shader != null) {
            process(Minecrft.get().getMainRenderTarget());
        }
    }

    /**
     * Processes current shader (if it is present and active) to a specified render target.
     * Current shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(RenderTarget renderTarget) {
        if (shader != null) {
            process(shader, renderTarget);
        }
    }

    /**
     * Processes specified shader (if it is present and active) to a specified render target.
     * Shader is not modified in the process. Copy of the shader is created and resized to the render target dimensions.
     * Since this method creates a temp PostChain on every call, this probably should not be used when performance matters.
     * Main use for this is to apply a shader when capturing a photograph.
     */
    public static void process(@NotNull Identifier shader, @NotNull RenderTarget renderTarget) {
        PostChain postChain = Minecrft.get().getShaderManager().getPostChain(shader, LevelTargetBundle.MAIN_TARGETS);
        if (postChain != null) {
            try (CrossFrameResourcePool allocator = new CrossFrameResourcePool(3)) {
                postChain.process(renderTarget, allocator);
            } catch (Exception e) {
                Exposure.LOGGER.warn("Failed to process shader: {}", shader, e);
            }
        } else {
            Exposure.LOGGER.warn("[FilterDebug] postChain NULL (not compiled/loaded) for shader={}", shader);
        }
    }

    public static void remove() {
        shader = null;
    }
}

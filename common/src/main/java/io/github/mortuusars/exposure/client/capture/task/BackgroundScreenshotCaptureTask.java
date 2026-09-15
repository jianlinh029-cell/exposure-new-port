package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PanoramicScreenshotParameters;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Off-screen ("background") capture for Minecraft 1.21.11.
 * <p>
 * Even though the world renderer is now driven by a FrameGraph, the main colour output of that graph is still
 * imported from {@code Minecraft#getMainRenderTarget()}
 * ({@code LevelRenderer.renderLevel}: {@code frameGraph.importExternal("main", minecraft.getMainRenderTarget())}).
 * {@code MinecraftMixin} redirects that call while {@link #capturing} is true, so by re-running
 * {@link GameRenderer#renderLevel} against a dedicated {@link TextureTarget} we render the world a second time,
 * fully off-screen and without any GUI/HUD/viewfinder, exactly like the official 1.21.1 implementation did by
 * binding a custom framebuffer. The FrameGraph's own "clear" pass clears colour + depth of the imported target,
 * so no manual clear/bindWrite (removed in 1.21.11) is required.
 */
public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    private static boolean capturing = false;

    @Nullable
    private static RenderTarget renderTarget = null;

    public static boolean isCapturing() {
        return capturing && renderTarget != null;
    }

    public static @NotNull RenderTarget getRenderTarget() {
        return Objects.requireNonNull(renderTarget);
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        if (ExposureClient.shouldUseDirectCapture()) {
            Exposure.LOGGER.warn("BackgroundScreenshotCaptureMethod is used while incompatible mods are installed. Captured image most likely will not look as expected.");
        }

        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer gameRenderer = minecraft.gameRenderer;
        // capturing is still false here, so this returns the real main target (used only for dimensions).
        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        TextureTarget target = new TextureTarget("exposure_background_capture",
                mainTarget.width, mainTarget.height, true);
        renderTarget = target;

        CompletableFuture<Result<Image>> future = new CompletableFuture<>();
        boolean panoramic = Config.Client.BACKGROUND_CAPTURE_USE_PANORAMIC_MODE.get();

        try {
            // From here on MinecraftMixin redirects every getMainRenderTarget() to our off-screen target.
            capturing = true;

            if (panoramic) {
                gameRenderer.setPanoramicScreenshotParameters(
                        new PanoramicScreenshotParameters(gameRenderer.getMainCamera().forwardVector()));
            }

            gameRenderer.setRenderBlockOutline(false);

            // Re-initialise the main camera before the off-screen re-render. SetCameraEntityAction calls
            // Camera#reset(), which clears the camera entity; renderLevel -> extractCamera then dereferences
            // mainCamera.entity() and NPEs unless the camera is set up again first (mirrors vanilla panorama).
            gameRenderer.updateCamera(minecraft.getDeltaTracker());

            // 1.21.11: Camera#reset() (called by SetCameraEntityAction#beforeCapture) also clears the
            // EnvironmentAttributeProbe. updateCamera() only runs Camera#setup(), which restores level/entity/
            // position/rotation but does NOT refresh the probe; only Camera#tick() calls attributeProbe.tick().
            // SkyRenderer#extractRenderState reads sky colour / sun / moon / star angles from that probe, so
            // without this tick the off-screen re-render has no environment attributes and the sky renders black.
            // Safe to call here: setup() above has already restored the camera entity, which tick() requires.
            gameRenderer.getMainCamera().tick();

            // Re-render the world (terrain, block entities incl. the FlashBlock light level=15, entities,
            // particles, held item) into the hijacked main target. GUI/HUD/viewfinder are never part of renderLevel.
            gameRenderer.renderLevel(minecraft.getDeltaTracker());

            applyShaderEffects(target);

            // Asynchronous GPU readback: the colour texture is copied into an isolated buffer during this call,
            // so the dedicated target only has to stay alive until the consumer runs (see releaseTarget).
            Screenshot.takeScreenshot(target, nativeImage -> {
                releaseTarget(target);
                future.complete(Result.success(new WrappedNativeImage(nativeImage)));
            });
        } catch (Exception e) {
            Exposure.LOGGER.error("Couldn't capture image: ", e);
            releaseTarget(target);
            future.complete(Result.error(Capture.ERROR_FAILED_GENERIC));
        } finally {
            if (panoramic) {
                gameRenderer.setPanoramicScreenshotParameters(null);
            }
            gameRenderer.setRenderBlockOutline(true);
            // Stop hijacking immediately; the normal frame must render to the real main target again.
            capturing = false;
        }

        return future;
    }

    @Override
    public void tick() {
        // The off-screen re-render is performed synchronously in execute(); the async readback completes the future.
    }

    private void applyShaderEffects(RenderTarget target) {
        // Re-apply the currently active global post effect on the dedicated target, mirroring vanilla render.
        Identifier effectId = Minecraft.getInstance().gameRenderer.currentPostEffect();
        if (effectId != null) {
            PostChain chain = Minecraft.getInstance().getShaderManager()
                    .getPostChain(effectId, LevelTargetBundle.MAIN_TARGETS);
            if (chain != null) {
                try (CrossFrameResourcePool pool = new CrossFrameResourcePool(3)) {
                    chain.process(target, pool);
                } catch (Exception e) {
                    Exposure.LOGGER.warn("Failed to process active post effect '{}' on capture.", effectId, e);
                }
            }
        }

        // Exposure capture filter (black & white / coloured glass / etc.).
        CaptureShader.process(target);
    }

    private static void releaseTarget(@Nullable RenderTarget target) {
        if (target == null) {
            return;
        }

        // RenderTarget GPU resources must be released on the render thread; the screenshot callback may run off it.
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            try {
                target.destroyBuffers();
            } catch (Exception e) {
                Exposure.LOGGER.warn("Failed to release background capture render target.", e);
            }
        });

        if (renderTarget == target) {
            renderTarget = null;
        }
    }
}

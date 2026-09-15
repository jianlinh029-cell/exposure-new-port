package io.github.mortuusars.exposure.fabric.mixin;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When the camera is mounted on a CameraStandEntity the vanilla entity extraction skips the LocalPlayer
 * (because the camera entity is not the player). Re-add the player's render state so the player's own body
 * is visible from the camera stand viewpoint.
 *
 * 1.21.11: world rendering was split into extract (render-state) / submit (FrameGraph) phases; the old
 * synchronous renderEntity(...) shadow method no longer exists.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    protected abstract EntityRenderState extractEntity(Entity entity, float partialTick);

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void exposure$renderCameraStandView(Camera camera, Frustum frustum, DeltaTracker deltaTracker,
                                               LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!(camera.entity() instanceof CameraStandEntity)) return;
        LocalPlayer player = Minecrft.player();
        if (player == null) return;

        if (player.tickCount == 0) {
            player.xOld = player.getX();
            player.yOld = player.getY();
            player.zOld = player.getZ();
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(!Minecrft.level().tickRateManager().isEntityFrozen(player));
        levelRenderState.entityRenderStates.add(this.extractEntity(player, partialTick));
    }
}

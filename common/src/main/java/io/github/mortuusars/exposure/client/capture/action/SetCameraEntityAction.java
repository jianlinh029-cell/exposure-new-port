package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.world.entity.Entity;

public class SetCameraEntityAction implements CaptureAction {
    private final Entity cameraEntity;
    private Entity cameraEntityBeforeCapture;

    public SetCameraEntityAction(Entity cameraEntity) {
        this.cameraEntity = cameraEntity;
        this.cameraEntityBeforeCapture = Minecrft.player();
    }

    @Override
    public void beforeCapture() {
        cameraEntityBeforeCapture = Minecrft.get().getCameraEntity();
        CameraClient.setCameraEntity(cameraEntity);
    }

    @Override
    public void afterCapture() {
        CameraClient.setCameraEntity(cameraEntityBeforeCapture);
    }
}

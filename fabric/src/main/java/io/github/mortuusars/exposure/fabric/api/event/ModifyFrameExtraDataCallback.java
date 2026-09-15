package io.github.mortuusars.exposure.fabric.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Can be used to add additional data to the frame or modify existing data. This data can be used in advancements or quests afterward.
 * Fired only on the server side.
 */
public interface ModifyFrameExtraDataCallback {
    Event<ModifyFrameExtraDataCallback> EVENT = EventFactory.createArrayBacked(ModifyFrameExtraDataCallback.class,
            (listeners) -> (cameraHolder, camera, captureProperties, positionsInFrame, entitiesInFrame, data) -> {
                for (ModifyFrameExtraDataCallback listener : listeners) {
                    listener.modifyFrameExtraData(cameraHolder, camera, captureProperties, positionsInFrame, entitiesInFrame, data);
                }
            });

    void modifyFrameExtraData(CameraHolder cameraHolder, ItemStack stack, CaptureParameters captureParameters,
                              List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data);
}

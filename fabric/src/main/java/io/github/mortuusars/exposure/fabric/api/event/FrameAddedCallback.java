package io.github.mortuusars.exposure.fabric.api.event;

import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Fired at the very end of a shot, when frame is added to the film.
 * Fired only on the server side.
 */
public interface FrameAddedCallback {
    Event<FrameAddedCallback> EVENT = EventFactory.createArrayBacked(FrameAddedCallback.class,
            (listeners) -> (cameraHolder, camera, frame, positionsInFrame, entitiesInFrame) -> {
                for (FrameAddedCallback listener : listeners) {
                    listener.frameAdded(cameraHolder, camera, frame, positionsInFrame, entitiesInFrame);
                }
            });

    void frameAdded(CameraHolder cameraHolder, ItemStack camera, Frame frame, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame);
}
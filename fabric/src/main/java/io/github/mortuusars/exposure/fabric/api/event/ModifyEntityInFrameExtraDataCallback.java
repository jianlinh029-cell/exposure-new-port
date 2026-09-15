package io.github.mortuusars.exposure.fabric.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Can be used to add custom entity in frame NBT data that is stored in a frame.
 */
public interface ModifyEntityInFrameExtraDataCallback {
    Event<ModifyEntityInFrameExtraDataCallback> EVENT = EventFactory.createArrayBacked(ModifyEntityInFrameExtraDataCallback.class,
            (listeners) -> (cameraHolder, camera, entityInFrame, data) -> {
                for (ModifyEntityInFrameExtraDataCallback listener : listeners) {
                    if (listener.modifyEntityInFrameData(cameraHolder, camera, entityInFrame, data))
                        return true;
                }

                return false;
            });

    boolean modifyEntityInFrameData(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data);
}